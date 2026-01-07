package dat.controllers.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import dat.controllers.IController;
import dat.daos.impl.*;
import dat.dtos.PaymentDTO;
import dat.entities.*;
import dat.enums.ActivityLogStatus;
import dat.enums.ActivityLogType;
import dat.enums.Currency;
import dat.enums.PaymentStatus;
import dat.enums.ReceiptStatus;
import dat.services.PaymentService;
import dat.services.StripePaymentService;
import dat.services.SubscriptionService;
import dat.utils.DateTimeUtil;
import dat.utils.ErrorResponse;
import io.javalin.http.Context;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for Payment endpoints
 * Handles payment processing with Stripe integration
 */
public class PaymentController implements IController<PaymentDTO> {
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private final PaymentDAO paymentDAO;
    private final PaymentMethodDAO paymentMethodDAO;
    private final CustomerDAO customerDAO;
    private final SubscriptionDAO subscriptionDAO;
    private final ProductDAO productDAO;
    private final ReceiptDAO receiptDAO;
    private final ActivityLogDAO activityLogDAO;
    private final SessionDAO sessionDAO;
    private final SmsBalanceDAO smsBalanceDAO;
    private final StripePaymentService stripeService;
    private final SubscriptionService subscriptionService;
    private final PaymentService paymentService; 

    public PaymentController(EntityManagerFactory emf) {
        this.paymentDAO = PaymentDAO.getInstance(emf);
        this.paymentMethodDAO = PaymentMethodDAO.getInstance(emf);
        this.customerDAO = CustomerDAO.getInstance(emf);
        this.subscriptionDAO = SubscriptionDAO.getInstance(emf);
        this.productDAO = ProductDAO.getInstance(emf);
        this.receiptDAO = ReceiptDAO.getInstance(emf);
        this.activityLogDAO = ActivityLogDAO.getInstance(emf);
        this.sessionDAO = SessionDAO.getInstance(emf);
        this.smsBalanceDAO = SmsBalanceDAO.getInstance(emf);
        this.stripeService = StripePaymentService.getInstance();
        this.subscriptionService = SubscriptionService.getInstance(emf);
        this.paymentService = PaymentService.getInstance(emf); 
    }

    /**
     * Add payment method (save card)
     * POST /api/payment-methods
     * 
     * SECURITY WARNING - FOR TESTING ONLY!
     * Production should use Stripe.js to tokenize cards on frontend.
     */
    public void addPaymentMethod(Context ctx) {
        try {
            // Parse request
            ObjectNode request = ctx.bodyAsClass(ObjectNode.class);
            Long customerId = request.get("customerId").asLong();
            String cardNumber = request.get("cardNumber").asText();
            Long expMonth = request.get("expMonth").asLong();
            Long expYear = request.get("expYear").asLong();
            String cvc = request.get("cvc").asText();
            boolean isDefault = request.has("isDefault") && request.get("isDefault").asBoolean();

            // Get customer
            Customer customer = customerDAO.getById(customerId)
                    .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

            // Create payment method in Stripe
            PaymentMethod stripePaymentMethod = stripeService.createPaymentMethod(
                    cardNumber, expMonth, expYear, cvc
            );

            // Save to database
            dat.entities.PaymentMethod paymentMethod = new dat.entities.PaymentMethod(
                    customer,
                    stripePaymentMethod.getType(),
                    stripePaymentMethod.getCard().getBrand(),
                    stripePaymentMethod.getCard().getLast4(),
                    stripePaymentMethod.getCard().getExpMonth().intValue(),
                    stripePaymentMethod.getCard().getExpYear().intValue(),
                    stripePaymentMethod.getId(),
                    isDefault,
                    dat.enums.PaymentMethodStatus.ACTIVE,
                    stripePaymentMethod.getCard().getFingerprint()
            );

            paymentMethodDAO.create(paymentMethod);
            logger.info("Payment method added for customer: {}", customerId);

            // Log activity
            Session session = getSessionFromContext(ctx);
            if (session != null) {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("paymentMethodId", paymentMethod.getId());
                metadata.put("brand", paymentMethod.getBrand());
                metadata.put("last4", paymentMethod.getLast4());
                metadata.put("isDefault", isDefault);
                
                ActivityLog activityLog = new ActivityLog(
                    customer,
                    session,
                    ActivityLogType.ADD_CARD,
                    ActivityLogStatus.SUCCESS,
                    metadata
                );
                activityLogDAO.create(activityLog);
            }

            ObjectNode response = objectMapper.createObjectNode()
                    .put("msg", "Payment method added successfully")
                    .put("paymentMethodId", paymentMethod.getId())
                    .put("brand", paymentMethod.getBrand())
                    .put("last4", paymentMethod.getLast4());
            
            ctx.status(201).json(response);

        } catch (StripeException e) {
            logger.error("Stripe error: {}", e.getMessage());
            ErrorResponse.badRequest(ctx, stripeService.getErrorMessage(e));
        } catch (IllegalArgumentException e) {
            ErrorResponse.notFound(ctx, e.getMessage());
        } catch (Exception e) {
            ErrorResponse.internalError(ctx, "Failed to add payment method", logger, e);
        }
    }

    /**
     * Process payment (charge card) with ACID guarantees
     * POST /api/payments
     * Body: { customerId, paymentMethodId (Long or String), amount, currency, description, subscriptionId?, productId? }
     * 
     * Supports two modes:
     * 1. paymentMethodId as Long: Uses saved payment method from database
     * 2. paymentMethodId as String (starts with "pm_"): Uses Stripe payment method ID directly (Stripe Elements)
     * 
     * ACID Implementation:
     * - All database operations happen in a single transaction via PaymentService
     * - If any step fails, all changes are rolled back automatically
     * - Ensures data consistency across Payment, Receipt, SMS Balance, Subscription, and Activity Logs
     */
    @Override
    public void create(Context ctx) {
        try {
            // Parse request
            ObjectNode request = ctx.bodyAsClass(ObjectNode.class);
            Long customerId = request.get("customerId").asLong();
            String paymentMethodIdStr = request.get("paymentMethodId").asText();
            Integer amountCents = request.get("amount").asInt();
            String currencyStr = request.get("currency").asText();
            String description = request.has("description") ? request.get("description").asText() : null;
            Long subscriptionId = request.has("subscriptionId") ? request.get("subscriptionId").asLong() : null;
            Long productId = request.has("productId") ? request.get("productId").asLong() : null;

            logger.info("Processing ACID payment for customer: {}, amount: {} {}", customerId, amountCents, currencyStr);

            // Get session for activity logging
            Session session = getSessionFromContext(ctx);

            // Build payment request
            PaymentService.PaymentRequest paymentRequest = new PaymentService.PaymentRequest(
                customerId,
                paymentMethodIdStr,
                amountCents,
                currencyStr,
                description,
                subscriptionId,
                productId,
                session
            );

            // Process payment with ACID guarantees (single transaction)
            PaymentService.PaymentResult result = paymentService.processPayment(paymentRequest);

            // Build response
            ObjectNode response = objectMapper.createObjectNode()
                    .put("msg", result.message)
                    .put("paymentId", result.payment.getId())
                    .put("status", result.payment.getStatus().toString())
                    .put("amount", result.payment.getPriceCents())
                    .put("currency", result.payment.getCurrency().toString())
                    .put("receiptId", result.receipt.getId())
                    .put("receiptNumber", result.receipt.getReceiptNumber());
            
            // Include next billing date if subscription payment
            if (result.subscription != null) {
                response.put("subscriptionId", result.subscription.getId());
                response.put("nextBillingDate", result.subscription.getNextBillingDate() != null ? 
                    result.subscription.getNextBillingDate().toString() : null);
            }
            
            ctx.status(201).json(response);
            logger.info("ACID payment completed successfully: Payment ID {}", result.payment.getId());

        } catch (PaymentService.PaymentProcessingException e) {
            logger.error("Payment processing failed: {}", e.getMessage());
            ErrorResponse.badRequest(ctx, e.getMessage());
        } catch (IllegalArgumentException e) {
            ErrorResponse.notFound(ctx, e.getMessage());
        } catch (Exception e) {
            ErrorResponse.internalError(ctx, "Payment failed", logger, e);
        }
    }

    /**
     * GET /api/payments/{id}
     * Get payment by ID
     */
    @Override
    public void read(Context ctx) {
        try {
            Long id = Long.parseLong(ctx.pathParam("id"));
            Payment payment = paymentDAO.getById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

            PaymentDTO dto = convertToDTO(payment);
            ctx.status(200).json(dto);
            logger.info("Retrieved payment ID: {}", id);

        } catch (NumberFormatException e) {
            ErrorResponse.badRequest(ctx, "Invalid payment ID format");
        } catch (IllegalArgumentException e) {
            ErrorResponse.notFound(ctx, e.getMessage());
        } catch (Exception e) {
            ErrorResponse.internalError(ctx, "Error retrieving payment", logger, e);
        }
    }

    /**
     * GET /api/customers/{customerId}/payments
     * Get all payments for a customer
     */
    public void getCustomerPayments(Context ctx) {
        try {
            Long customerId = Long.parseLong(ctx.pathParam("customerId"));
            Set<Payment> payments = paymentDAO.getByCustomerId(customerId);
            
            List<PaymentDTO> dtos = payments.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            ctx.status(200).json(dtos);
            logger.info("Retrieved {} payments for customer ID: {}", dtos.size(), customerId);

        } catch (NumberFormatException e) {
            ErrorResponse.badRequest(ctx, "Invalid customer ID format");
        } catch (Exception e) {
            ErrorResponse.internalError(ctx, "Error retrieving payments", logger, e);
        }
    }

    /**
     * GET /api/payments/{paymentId}/receipt
     * Get receipt for a payment
     */
    public void getReceipt(Context ctx) {
        try {
            Long paymentId = Long.parseLong(ctx.pathParam("paymentId"));
            Receipt receipt = receiptDAO.getByPaymentId(paymentId)
                    .orElseThrow(() -> new IllegalArgumentException("Receipt not found"));

            ctx.status(200).json(receipt);
            logger.info("Retrieved receipt for payment ID: {}", paymentId);

        } catch (NumberFormatException e) {
            ErrorResponse.badRequest(ctx, "Invalid payment ID format");
        } catch (IllegalArgumentException e) {
            ErrorResponse.notFound(ctx, e.getMessage());
        } catch (Exception e) {
            ErrorResponse.internalError(ctx, "Error retrieving receipt", logger, e);
        }
    }

    // ==================== Helper Methods ====================

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

    private PaymentDTO convertToDTO(Payment payment) {
        PaymentDTO dto = new PaymentDTO();
        dto.id = payment.getId();
        dto.customerId = payment.getCustomer().getId();
        dto.paymentMethodId = payment.getPaymentMethod() != null ? payment.getPaymentMethod().getId() : null;
        dto.subscriptionId = payment.getSubscription() != null ? payment.getSubscription().getId() : null;
        dto.productId = payment.getProduct() != null ? payment.getProduct().getId() : null;
        dto.status = payment.getStatus();
        dto.priceCents = payment.getPriceCents();
        dto.currency = payment.getCurrency();
        dto.processorIntentId = payment.getProcessorIntentId();
        dto.createdAt = payment.getCreatedAt();
        return dto;
    }

    /**
     * Helper method to get session from JWT token in context
     */
    private Session getSessionFromContext(Context ctx) {
        try {
            String token = ctx.header("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                return sessionDAO.findByToken(token).orElse(null);
            }
        } catch (Exception e) {
            logger.warn("Could not retrieve session from context: {}", e.getMessage());
        }
        return null;
    }

    // ==================== IController Interface ====================

    @Override
    public void readAll(Context ctx) {
        ErrorResponse.notImplemented(ctx, "Use customer-specific endpoint: GET /api/customers/{id}/payments");
    }

    /**
     * GET /api/customers/{customerId}/payment-methods
     * Get all payment methods for a customer
     */
    public void getCustomerPaymentMethods(Context ctx) {
        try {
            Long customerId = Long.parseLong(ctx.pathParam("customerId"));
            
            // Get customer
            Customer customer = customerDAO.getById(customerId)
                    .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
            
            // Get payment methods
            Set<dat.entities.PaymentMethod> paymentMethods = paymentMethodDAO.getByCustomer(customer);
            
            // Convert to DTOs
            List<dat.dtos.PaymentMethodDTO> dtos = paymentMethods.stream()
                    .map(pm -> {
                        dat.dtos.PaymentMethodDTO dto = new dat.dtos.PaymentMethodDTO();
                        dto.id = pm.getId();
                        dto.customerId = pm.getCustomer().getId();
                        dto.type = pm.getType();
                        dto.brand = pm.getBrand();
                        dto.last4 = pm.getLast4();
                        dto.expMonth = pm.getExpMonth();
                        dto.expYear = pm.getExpYear();
                        dto.processorMethodId = pm.getProcessorMethodId();
                        dto.isDefault = pm.getIsDefault();
                        dto.status = pm.getStatus();
                        return dto;
                    })
                    .sorted((a, b) -> Boolean.compare(b.isDefault, a.isDefault)) // Default first
                    .collect(Collectors.toList());
            
            ctx.status(200).json(dtos);
            logger.info("Retrieved {} payment methods for customer ID: {}", dtos.size(), customerId);
            
        } catch (NumberFormatException e) {
            ErrorResponse.badRequest(ctx, "Invalid customer ID format");
        } catch (IllegalArgumentException e) {
            ErrorResponse.notFound(ctx, e.getMessage());
        } catch (Exception e) {
            ErrorResponse.internalError(ctx, "Error retrieving payment methods", logger, e);
        }
    }

    @Override
    public void update(Context ctx) {
        ErrorResponse.notImplemented(ctx, "Payments cannot be updated once created");
    }

    @Override
    public void delete(Context ctx) {
        ErrorResponse.notImplemented(ctx, "Payments cannot be deleted");
    }
}

