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
import dat.enums.Currency;
import dat.enums.PaymentStatus;
import dat.enums.ReceiptStatus;
import dat.services.StripePaymentService;
import dat.services.SubscriptionService;
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
    private final StripePaymentService stripeService;
    private final SubscriptionService subscriptionService;

    public PaymentController(EntityManagerFactory emf) {
        this.paymentDAO = PaymentDAO.getInstance(emf);
        this.paymentMethodDAO = PaymentMethodDAO.getInstance(emf);
        this.customerDAO = CustomerDAO.getInstance(emf);
        this.subscriptionDAO = SubscriptionDAO.getInstance(emf);
        this.productDAO = ProductDAO.getInstance(emf);
        this.receiptDAO = ReceiptDAO.getInstance(emf);
        this.stripeService = StripePaymentService.getInstance();
        this.subscriptionService = SubscriptionService.getInstance(emf);
    }

    /**
     * Add payment method (save card)
     * POST /api/payment-methods
     * Body: { customerId, cardNumber, expMonth, expYear, cvc, isDefault }
     * 
     * ⚠️ SECURITY WARNING - FOR TESTING/DEVELOPMENT ONLY!
     * This endpoint accepts raw card numbers for testing purposes.
     * 
     * IN PRODUCTION, you MUST:
     * 1. Remove this endpoint or restrict to internal use only
     * 2. Use Stripe.js on frontend to tokenize cards
     * 3. Create endpoint that accepts Stripe tokens instead of card numbers
     * 4. Never send raw card numbers through your backend
     * 
     * See: https://stripe.com/docs/payments/accept-a-payment
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

            ObjectNode response = objectMapper.createObjectNode()
                    .put("msg", "Payment method added successfully")
                    .put("paymentMethodId", paymentMethod.getId())
                    .put("brand", paymentMethod.getBrand())
                    .put("last4", paymentMethod.getLast4());
            
            ctx.status(201).json(response);

        } catch (StripeException e) {
            logger.error("Stripe error: {}", e.getMessage());
            ctx.status(400).json("{\"msg\": \"" + stripeService.getErrorMessage(e) + "\"}");
        } catch (IllegalArgumentException e) {
            ctx.status(404).json("{\"msg\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("Error adding payment method", e);
            ctx.status(500).json("{\"msg\": \"Failed to add payment method: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Process payment (charge card)
     * POST /api/payments
     * Body: { customerId, paymentMethodId, amount, currency, description, subscriptionId?, productId? }
     */
    @Override
    public void create(Context ctx) {
        try {
                // Parse request
                ObjectNode request = ctx.bodyAsClass(ObjectNode.class);
                Long customerId = request.get("customerId").asLong();
                Long paymentMethodId = request.get("paymentMethodId").asLong();
                Integer amountCents = request.get("amount").asInt();
                String currencyStr = request.get("currency").asText();
                String description = request.has("description") ? request.get("description").asText() : null;
                Long subscriptionId = request.has("subscriptionId") ? request.get("subscriptionId").asLong() : null;
                Long productId = request.has("productId") ? request.get("productId").asLong() : null;

                // Get entities
                Customer customer = customerDAO.getById(customerId)
                        .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
                
                dat.entities.PaymentMethod paymentMethod = paymentMethodDAO.getById(paymentMethodId)
                        .orElseThrow(() -> new IllegalArgumentException("Payment method not found"));

                Subscription subscription = subscriptionId != null ? 
                        subscriptionDAO.getById(subscriptionId).orElse(null) : null;
                
                Product product = productId != null ? 
                        productDAO.getById(productId).orElse(null) : null;

                // Create payment in Stripe
                Map<String, String> metadata = new HashMap<>();
                metadata.put("customer_id", customerId.toString());
                if (subscriptionId != null) metadata.put("subscription_id", subscriptionId.toString());
                if (productId != null) metadata.put("product_id", productId.toString());

                PaymentIntent paymentIntent = stripeService.createPaymentIntent(
                        amountCents.longValue(),
                        currencyStr,
                        paymentMethod.getProcessorMethodId(),
                        description,
                        metadata
                );

                // Determine payment status
                PaymentStatus status = stripeService.isPaymentSuccessful(paymentIntent) ? 
                        PaymentStatus.COMPLETED : PaymentStatus.PENDING;

                // Save payment to database
                Payment payment = new Payment(
                        customer,
                        paymentMethod,
                        subscription,
                        product,
                        status,
                        amountCents,
                        Currency.valueOf(currencyStr.toUpperCase()),
                        paymentIntent.getId()
                );
                paymentDAO.create(payment);

                // Generate receipt if payment successful
                Receipt receipt = null;
                if (status == PaymentStatus.COMPLETED) {
                    receipt = generateReceipt(payment, paymentIntent);
                    receiptDAO.create(receipt);
                    logger.info("Receipt generated: {}", receipt.getReceiptNumber());
                }

                // Update subscription after successful payment
                if (subscription != null && status == PaymentStatus.COMPLETED) {
                    subscriptionService.updateSubscriptionAfterPayment(subscription, payment);
                    logger.info("Subscription {} updated with new billing date: {}", 
                        subscription.getId(), subscription.getNextBillingDate());
                }

                logger.info("Payment created: {} with status: {}", payment.getId(), status);

                ObjectNode response = objectMapper.createObjectNode()
                        .put("msg", "Payment processed successfully")
                        .put("paymentId", payment.getId())
                        .put("status", status.toString())
                        .put("amount", amountCents)
                        .put("currency", currencyStr)
                        .put("receiptId", receipt != null ? receipt.getId() : null)
                        .put("receiptNumber", receipt != null ? receipt.getReceiptNumber() : null);
                
                // Include next billing date if subscription payment
                if (subscription != null) {
                    response.put("subscriptionId", subscription.getId());
                    response.put("nextBillingDate", subscription.getNextBillingDate() != null ? 
                        subscription.getNextBillingDate().toString() : null);
                }
                
                ctx.status(201).json(response);

        } catch (StripeException e) {
            logger.error("Stripe payment error: {}", e.getMessage());
            ctx.status(400).json("{\"msg\": \"" + stripeService.getErrorMessage(e) + "\"}");
        } catch (IllegalArgumentException e) {
            ctx.status(404).json("{\"msg\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("Error processing payment", e);
            ctx.status(500).json("{\"msg\": \"Payment failed: " + e.getMessage() + "\"}");
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
            ctx.status(400).json("{\"msg\": \"Invalid payment ID format\"}");
        } catch (IllegalArgumentException e) {
            ctx.status(404).json("{\"msg\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("Error retrieving payment", e);
            ctx.status(500).json("{\"msg\": \"Error retrieving payment: " + e.getMessage() + "\"}");
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
            ctx.status(400).json("{\"msg\": \"Invalid customer ID format\"}");
        } catch (Exception e) {
            logger.error("Error retrieving customer payments", e);
            ctx.status(500).json("{\"msg\": \"Error retrieving payments: " + e.getMessage() + "\"}");
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
            ctx.status(400).json("{\"msg\": \"Invalid payment ID format\"}");
        } catch (IllegalArgumentException e) {
            ctx.status(404).json("{\"msg\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("Error retrieving receipt", e);
            ctx.status(500).json("{\"msg\": \"Error retrieving receipt: " + e.getMessage() + "\"}");
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
        
        return new Receipt(
                payment,
                receiptNumber,
                payment.getPriceCents(),
                OffsetDateTime.now(),
                ReceiptStatus.PAID,
                receiptUrl,
                payment.getCustomer().getUser().getEmail(),
                payment.getCustomer().getCompanyName(),
                payment.getPaymentMethod().getBrand(),
                payment.getPaymentMethod().getLast4(),
                payment.getPaymentMethod().getExpYear(),
                paymentIntent.getId(),
                new HashMap<>()
        );
    }

    private PaymentDTO convertToDTO(Payment payment) {
        PaymentDTO dto = new PaymentDTO();
        dto.id = payment.getId();
        dto.customerId = payment.getCustomer().getId();
        dto.paymentMethodId = payment.getPaymentMethod().getId();
        dto.subscriptionId = payment.getSubscription() != null ? payment.getSubscription().getId() : null;
        dto.productId = payment.getProduct() != null ? payment.getProduct().getId() : null;
        dto.status = payment.getStatus();
        dto.priceCents = payment.getPriceCents();
        dto.currency = payment.getCurrency();
        dto.processorIntentId = payment.getProcessorIntentId();
        dto.createdAt = payment.getCreatedAt();
        return dto;
    }

    // ==================== IController Interface ====================

    @Override
    public void readAll(Context ctx) {
        ctx.status(501).json("{\"msg\": \"Use customer-specific endpoint: GET /api/customers/{id}/payments\"}");
    }

    @Override
    public void update(Context ctx) {
        ctx.status(501).json("{\"msg\": \"Payments cannot be updated once created\"}");
    }

    @Override
    public void delete(Context ctx) {
        ctx.status(501).json("{\"msg\": \"Payments cannot be deleted\"}");
    }
}

