package dat.services;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import dat.daos.impl.*;
import dat.entities.*;
import dat.enums.*;
import dat.utils.DateTimeUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Service for handling complete payment processing with ACID guarantees
 * All payment operations are executed within a single database transaction
 * 
 * ACID Principles Implementation:
 * - Atomicity: All operations succeed or all fail (single transaction)
 * - Consistency: Validates all business rules before commit
 * - Isolation: Transaction isolation prevents concurrent conflicts
 * - Durability: Committed data persists permanently
 * 
 * @author NotionPay Team
 */
public class PaymentService {
    private static PaymentService instance;
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    
    private final EntityManagerFactory emf;
    private final PaymentDAO paymentDAO;
    private final PaymentMethodDAO paymentMethodDAO;
    private final CustomerDAO customerDAO;
    private final SubscriptionDAO subscriptionDAO;
    private final ProductDAO productDAO;
    private final ReceiptDAO receiptDAO;
    private final ActivityLogDAO activityLogDAO;
    private final SmsBalanceDAO smsBalanceDAO;
    private final StripePaymentService stripeService;
    private final SubscriptionService subscriptionService;

    public static PaymentService getInstance(EntityManagerFactory emf) {
        if (instance == null) {
            instance = new PaymentService(emf);
        }
        return instance;
    }

    private PaymentService(EntityManagerFactory emf) {
        this.emf = emf;
        this.paymentDAO = PaymentDAO.getInstance(emf);
        this.paymentMethodDAO = PaymentMethodDAO.getInstance(emf);
        this.customerDAO = CustomerDAO.getInstance(emf);
        this.subscriptionDAO = SubscriptionDAO.getInstance(emf);
        this.productDAO = ProductDAO.getInstance(emf);
        this.receiptDAO = ReceiptDAO.getInstance(emf);
        this.activityLogDAO = ActivityLogDAO.getInstance(emf);
        this.smsBalanceDAO = SmsBalanceDAO.getInstance(emf);
        this.stripeService = StripePaymentService.getInstance();
        this.subscriptionService = SubscriptionService.getInstance(emf);
        logger.info("PaymentService initialized with ACID transaction support");
    }

    /**
     * Process a complete payment with ACID guarantees
     * 
     * This method executes the entire payment flow within a single database transaction:
     * 1. Validate input and retrieve entities
     * 2. Process payment with Stripe (external, happens before DB transaction)
     * 3. Save payment record
     * 4. Generate receipt
     * 5. Update SMS balance (if applicable)
     * 6. Update subscription (if applicable)
     * 7. Log all activities
     * 
     * If ANY step fails, ALL database changes are rolled back automatically.
     * 
     * @param request Payment request with all necessary data
     * @return PaymentResult with payment details and status
     * @throws PaymentProcessingException if payment fails at any stage
     */
    public PaymentResult processPayment(PaymentRequest request) throws PaymentProcessingException {
        logger.info("Starting ACID payment processing for customer: {}, amount: {} {}", 
            request.customerId, request.amountCents, request.currency);
        
        EntityManager em = null;
        PaymentIntent paymentIntent = null;
        
        try {
            // ========== STEP 1: Validate and retrieve entities (READ-ONLY) ==========
            logger.debug("Step 1: Validating input and retrieving entities");
            
            Customer customer = customerDAO.getById(request.customerId)
                    .orElseThrow(() -> new PaymentProcessingException("Customer not found: " + request.customerId));
            
            // Determine payment method
            String stripePaymentMethodId;
            dat.entities.PaymentMethod savedPaymentMethod = null;
            boolean isOneTimePayment = request.paymentMethodId.startsWith("pm_");
            
            if (isOneTimePayment) {
                stripePaymentMethodId = request.paymentMethodId;
                logger.debug("Using one-time Stripe payment method: {}", stripePaymentMethodId);
            } else {
                Long paymentMethodId = Long.parseLong(request.paymentMethodId);
                savedPaymentMethod = paymentMethodDAO.getById(paymentMethodId)
                        .orElseThrow(() -> new PaymentProcessingException("Payment method not found: " + paymentMethodId));
                stripePaymentMethodId = savedPaymentMethod.getProcessorMethodId();
                logger.debug("Using saved payment method: {}", paymentMethodId);
            }
            
            // Get optional entities
            Subscription subscription = null;
            if (request.subscriptionId != null) {
                subscription = subscriptionDAO.getById(request.subscriptionId).orElse(null);
            }
            
            Product product = null;
            if (request.productId != null) {
                product = productDAO.getById(request.productId).orElse(null);
            }
            
            // ========== STEP 2: Process Stripe payment (EXTERNAL - before transaction) ==========
            logger.debug("Step 2: Processing Stripe payment");
            
            Map<String, String> stripeMetadata = new HashMap<>();
            stripeMetadata.put("customer_id", customer.getId().toString());
            stripeMetadata.put("one_time_payment", String.valueOf(isOneTimePayment));
            if (subscription != null) stripeMetadata.put("subscription_id", subscription.getId().toString());
            if (product != null) stripeMetadata.put("product_id", product.getId().toString());
            
            try {
                paymentIntent = stripeService.createPaymentIntent(
                    request.amountCents.longValue(),
                    request.currency,
                    stripePaymentMethodId,
                    request.description,
                    stripeMetadata
                );
            } catch (StripeException e) {
                logger.error("Stripe payment failed: {}", e.getMessage());
                throw new PaymentProcessingException("Stripe payment failed: " + stripeService.getErrorMessage(e), e);
            }
            
            // Check payment status
            PaymentStatus status = stripeService.isPaymentSuccessful(paymentIntent) ? 
                    PaymentStatus.COMPLETED : PaymentStatus.PENDING;
            
            if (status != PaymentStatus.COMPLETED) {
                throw new PaymentProcessingException("Payment not completed. Status: " + paymentIntent.getStatus());
            }
            
            // ========== STEP 3: Start database transaction (ALL OR NOTHING) ==========
            logger.debug("Step 3: Starting database transaction");
            em = emf.createEntityManager();
            em.getTransaction().begin();
            
            // ========== STEP 4: Save payment record ==========
            logger.debug("Step 4: Saving payment record");
            Payment payment = new Payment(
                customer,
                savedPaymentMethod,
                subscription,
                product,
                status,
                request.amountCents,
                Currency.valueOf(request.currency.toUpperCase()),
                paymentIntent.getId()
            );
            em.persist(payment);
            em.flush(); // Ensure payment gets ID
            logger.debug("Payment persisted with ID: {}", payment.getId());
            
            // ========== STEP 5: Generate receipt ==========
            logger.debug("Step 5: Generating receipt");
            Receipt receipt = generateReceipt(payment, paymentIntent);
            em.persist(receipt);
            em.flush(); // Ensure receipt gets ID
            logger.debug("Receipt generated: {}", receipt.getReceiptNumber());
            
            // ========== STEP 6: Update SMS balance (if applicable) ==========
            if (product != null && product.getSmsCount() != null) {
                logger.debug("Step 6: Updating SMS balance");
                String externalCustomerId = customer.getExternalCustomerId();
                int smsCredits = product.getSmsCount();
                
                // Recharge SMS credits within the same transaction
                SmsBalance smsBalance = em.createQuery(
                    "SELECT s FROM SmsBalance s WHERE s.externalCustomerId = :externalId",
                    SmsBalance.class
                )
                .setParameter("externalId", externalCustomerId)
                .getSingleResult();
                
                smsBalance.recharge(smsCredits);
                em.merge(smsBalance);
                logger.debug("SMS balance updated: added {} credits to customer {}", smsCredits, externalCustomerId);
                
                // Log SMS purchase activity
                if (request.session != null) {
                    Map<String, Object> smsMetadata = new HashMap<>();
                    smsMetadata.put("productId", product.getId());
                    smsMetadata.put("productName", product.getName());
                    smsMetadata.put("smsCreditsAdded", smsCredits);
                    smsMetadata.put("paymentId", payment.getId());
                    smsMetadata.put("oneTimePayment", isOneTimePayment);
                    
                    ActivityLog smsLog = new ActivityLog(
                        customer,
                        request.session,
                        ActivityLogType.SMS_PURCHASE,
                        ActivityLogStatus.SUCCESS,
                        smsMetadata
                    );
                    em.persist(smsLog);
                }
            }
            
            // ========== STEP 7: Update subscription (if applicable) ==========
            if (subscription != null) {
                logger.debug("Step 7: Updating subscription");
                
                // Calculate new billing date
                OffsetDateTime oldBillingDate = subscription.getNextBillingDate();
                OffsetDateTime newBillingDate = subscriptionService.calculateNextBillingDate(subscription);
                
                subscription.setNextBillingDate(newBillingDate);
                subscription.setStatus(SubscriptionStatus.ACTIVE);
                em.merge(subscription);
                
                logger.debug("Subscription {} updated: nextBillingDate changed from {} to {}", 
                    subscription.getId(), oldBillingDate, newBillingDate);
                
                // Log subscription renewal
                if (request.session != null) {
                    Map<String, Object> renewalMetadata = new HashMap<>();
                    renewalMetadata.put("subscriptionId", subscription.getId());
                    renewalMetadata.put("planId", subscription.getPlan().getId());
                    renewalMetadata.put("planName", subscription.getPlan().getName());
                    renewalMetadata.put("previousBillingDate", oldBillingDate.toString());
                    renewalMetadata.put("nextBillingDate", newBillingDate.toString());
                    renewalMetadata.put("paymentId", payment.getId());
                    
                    ActivityLog renewalLog = new ActivityLog(
                        customer,
                        request.session,
                        ActivityLogType.SUBSCRIPTION_RENEWED,
                        ActivityLogStatus.SUCCESS,
                        renewalMetadata
                    );
                    em.persist(renewalLog);
                }
            }
            
            // ========== STEP 8: Log payment activity ==========
            logger.debug("Step 8: Logging payment activity");
            if (request.session != null) {
                Map<String, Object> paymentMetadata = new HashMap<>();
                paymentMetadata.put("paymentId", payment.getId());
                paymentMetadata.put("amount", request.amountCents);
                paymentMetadata.put("currency", request.currency);
                paymentMetadata.put("status", status.toString());
                paymentMetadata.put("oneTimePayment", isOneTimePayment);
                if (subscription != null) {
                    paymentMetadata.put("subscriptionId", subscription.getId());
                }
                if (product != null) {
                    paymentMetadata.put("productId", product.getId());
                }
                
                ActivityLog activityLog = new ActivityLog(
                    customer,
                    request.session,
                    ActivityLogType.PAYMENT,
                    ActivityLogStatus.SUCCESS,
                    paymentMetadata
                );
                em.persist(activityLog);
            }
            
            // ========== STEP 9: Commit transaction (DURABILITY) ==========
            logger.debug("Step 9: Committing transaction");
            em.getTransaction().commit();
            logger.info("Payment processing completed successfully. Payment ID: {}, Receipt: {}", 
                payment.getId(), receipt.getReceiptNumber());
            
            // Return success result
            return new PaymentResult(
                true,
                payment,
                receipt,
                subscription,
                "Payment processed successfully"
            );
            
        } catch (PaymentProcessingException e) {
            // Business logic error - rollback and rethrow
            if (em != null && em.getTransaction().isActive()) {
                logger.warn("Rolling back transaction due to payment processing error: {}", e.getMessage());
                em.getTransaction().rollback();
            }
            throw e;
            
        } catch (Exception e) {
            // Unexpected error - rollback and wrap
            if (em != null && em.getTransaction().isActive()) {
                logger.error("Rolling back transaction due to unexpected error", e);
                em.getTransaction().rollback();
            }
            throw new PaymentProcessingException("Payment processing failed: " + e.getMessage(), e);
            
        } finally {
            // Always close EntityManager
            if (em != null) {
                em.close();
            }
        }
    }

    /**
     * Generate receipt for a payment
     */
    private Receipt generateReceipt(Payment payment, PaymentIntent paymentIntent) {
        String receiptNumber = "RCP-" + System.currentTimeMillis();
        
        // Get receipt URL from Stripe (if available)
        String receiptUrl = null;
        try {
            if (paymentIntent.getLatestCharge() != null) {
                com.stripe.model.Charge charge = com.stripe.model.Charge.retrieve(paymentIntent.getLatestCharge());
                receiptUrl = charge.getReceiptUrl();
            }
        } catch (Exception e) {
            logger.warn("Could not retrieve Stripe receipt URL: {}", e.getMessage());
        }
        
        // Build detailed metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("customerId", payment.getCustomer().getId());
        metadata.put("paymentId", payment.getId());
        metadata.put("currency", payment.getCurrency().toString());
        metadata.put("paymentStatus", payment.getStatus().toString());
        
        // Add subscription info if available
        if (payment.getSubscription() != null) {
            metadata.put("subscriptionId", payment.getSubscription().getId());
            metadata.put("planName", payment.getSubscription().getPlan().getName());
            metadata.put("billingPeriod", payment.getSubscription().getPlan().getPeriod().toString());
        }
        
        // Add product info if available
        if (payment.getProduct() != null) {
            metadata.put("productId", payment.getProduct().getId());
            metadata.put("productName", payment.getProduct().getName());
            metadata.put("productType", payment.getProduct().getProductType().toString());
            if (payment.getProduct().getSmsCount() != null) {
                metadata.put("smsCount", payment.getProduct().getSmsCount());
            }
        }
        
        // Handle null payment method (for one-time Stripe Elements payments)
        String brand = payment.getPaymentMethod() != null ? payment.getPaymentMethod().getBrand() : "Card";
        String last4 = payment.getPaymentMethod() != null ? payment.getPaymentMethod().getLast4() : "****";
        Integer expYear = payment.getPaymentMethod() != null ? payment.getPaymentMethod().getExpYear() : null;
        
        return new Receipt(
            payment,
            receiptNumber,
            payment.getPriceCents(),
            DateTimeUtil.now(),
            ReceiptStatus.PAID,
            receiptUrl,
            payment.getCustomer().getUser().getEmail(),
            payment.getCustomer().getCompanyName(),
            brand,
            last4,
            expYear,
            paymentIntent.getId(),
            metadata
        );
    }

    /**
     * Get payment by ID
     */
    public Optional<Payment> getById(Long id) {
        return paymentDAO.getById(id);
    }

    /**
     * Get all payments for a customer
     */
    public Set<Payment> getByCustomerId(Long customerId) {
        return paymentDAO.getByCustomerId(customerId);
    }

    // ==================== Inner Classes ====================

    /**
     * Payment request data transfer object
     */
    public static class PaymentRequest {
        public Long customerId;
        public String paymentMethodId; // Can be database ID or Stripe "pm_xxx" ID
        public Integer amountCents;
        public String currency;
        public String description;
        public Long subscriptionId; // Optional
        public Long productId; // Optional
        public Session session; // Optional, for activity logging

        public PaymentRequest(Long customerId, String paymentMethodId, Integer amountCents, 
                            String currency, String description, Long subscriptionId, 
                            Long productId, Session session) {
            this.customerId = customerId;
            this.paymentMethodId = paymentMethodId;
            this.amountCents = amountCents;
            this.currency = currency;
            this.description = description;
            this.subscriptionId = subscriptionId;
            this.productId = productId;
            this.session = session;
        }
    }

    /**
     * Payment result data transfer object
     */
    public static class PaymentResult {
        public boolean success;
        public Payment payment;
        public Receipt receipt;
        public Subscription subscription;
        public String message;

        public PaymentResult(boolean success, Payment payment, Receipt receipt, 
                           Subscription subscription, String message) {
            this.success = success;
            this.payment = payment;
            this.receipt = receipt;
            this.subscription = subscription;
            this.message = message;
        }
    }

    /**
     * Custom exception for payment processing errors
     */
    public static class PaymentProcessingException extends Exception {
        public PaymentProcessingException(String message) {
            super(message);
        }

        public PaymentProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

