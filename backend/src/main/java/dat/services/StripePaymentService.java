package dat.services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentMethodCreateParams;
import dat.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for handling Stripe payment operations
 * Provides secure integration with Stripe API for payment processing
 * 
 * @author NotionPay Team
 */
public class StripePaymentService {
    private static StripePaymentService instance;
    private static final Logger logger = LoggerFactory.getLogger(StripePaymentService.class);
    private static boolean initialized = false;


    public static StripePaymentService getInstance() {
        if (instance == null) {
            instance = new StripePaymentService();
            instance.initializeStripe();
        }
        return instance;
    }

    private StripePaymentService() {
        // Private constructor for singleton pattern
    }

    /**
     * Initialize Stripe with secret key from config
     * Only runs once during application lifetime
     */
    private void initializeStripe() {
        if (!initialized) {
            try {
                String secretKey;
                if (System.getenv("DEPLOYED") != null) {
                    secretKey = System.getenv("STRIPE_SECRET_KEY");
                } else {
                    secretKey = Utils.getPropertyValue("STRIPE_SECRET_KEY", "config.properties");
                }
                
                if (secretKey == null || secretKey.isEmpty()) {
                    throw new IllegalStateException("STRIPE_SECRET_KEY not found in configuration");
                }
                
                Stripe.apiKey = secretKey;
                initialized = true;
                logger.info("Stripe API initialized successfully");
                
            } catch (Exception e) {
                logger.error("Failed to initialize Stripe API", e);
                throw new RuntimeException("Stripe initialization failed", e);
            }
        }
    }

    /**
     * Create a PaymentMethod (save card) in Stripe
     * 
     * @param cardNumber Card number (e.g., "4242424242424242")
     * @param expMonth Expiration month (1-12)
     * @param expYear Expiration year (e.g., 2025)
     * @param cvc Card security code
     * @return Stripe PaymentMethod object containing payment method ID and card details
     * @throws StripeException if card validation fails or Stripe API error occurs
     */
    public PaymentMethod createPaymentMethod(String cardNumber, Long expMonth, Long expYear, String cvc) 
            throws StripeException {
        try {
            logger.info("Creating payment method in Stripe");
            
            PaymentMethodCreateParams params = PaymentMethodCreateParams.builder()
                .setType(PaymentMethodCreateParams.Type.CARD)
                .setCard(
                    PaymentMethodCreateParams.CardDetails.builder()
                        .setNumber(cardNumber)
                        .setExpMonth(expMonth)
                        .setExpYear(expYear)
                        .setCvc(cvc)
                        .build()
                )
                .build();

            PaymentMethod paymentMethod = PaymentMethod.create(params);
            logger.info("Payment method created successfully: {}", paymentMethod.getId());
            
            return paymentMethod;
            
        } catch (StripeException e) {
            logger.error("Failed to create payment method: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Attach a PaymentMethod to a Stripe Customer (for future use)
     * This is useful if you want to save the card for recurring payments
     * 
     * @param paymentMethodId Stripe PaymentMethod ID
     * @param stripeCustomerId Stripe Customer ID
     * @return Updated PaymentMethod
     * @throws StripeException if attachment fails
     */
    public PaymentMethod attachPaymentMethodToCustomer(String paymentMethodId, String stripeCustomerId) 
            throws StripeException {
        try {
            logger.info("Attaching payment method {} to customer {}", paymentMethodId, stripeCustomerId);
            
            PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);
            
            Map<String, Object> params = new HashMap<>();
            params.put("customer", stripeCustomerId);
            
            PaymentMethod attached = paymentMethod.attach(params);
            logger.info("Payment method attached successfully");
            
            return attached;
            
        } catch (StripeException e) {
            logger.error("Failed to attach payment method: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Create and confirm a payment (charge a card)
     * 
     * @param amountCents Amount in cents (e.g., 1000 = $10.00 or 10 DKK)
     * @param currency Currency code (e.g., "dkk", "usd", "eur")
     * @param paymentMethodId Stripe PaymentMethod ID
     * @param description Payment description (e.g., "Subscription payment for Basic Plan")
     * @param metadata Additional metadata to store with payment
     * @return Stripe PaymentIntent object with payment status and details
     * @throws StripeException if payment fails or Stripe API error occurs
     */
    public PaymentIntent createPaymentIntent(
            Long amountCents, 
            String currency, 
            String paymentMethodId,
            String description,
            Map<String, String> metadata) throws StripeException {
        
        try {
            logger.info("Creating payment intent for amount: {} {}", amountCents, currency.toUpperCase());
            
            PaymentIntentCreateParams.Builder builder = PaymentIntentCreateParams.builder()
                .setAmount(amountCents)
                .setCurrency(currency.toLowerCase())
                .setPaymentMethod(paymentMethodId)
                .setConfirm(true)  // Automatically confirm the payment
                .setAutomaticPaymentMethods(
                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                        .setEnabled(true)
                        .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                        .build()
                );
            
            if (description != null && !description.isEmpty()) {
                builder.setDescription(description);
            }
            
            if (metadata != null && !metadata.isEmpty()) {
                builder.putAllMetadata(metadata);
            }
            
            PaymentIntent paymentIntent = PaymentIntent.create(builder.build());
            
            logger.info("Payment intent created: {} with status: {}", 
                paymentIntent.getId(), paymentIntent.getStatus());
            
            return paymentIntent;
            
        } catch (StripeException e) {
            logger.error("Failed to create payment intent: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieve an existing PaymentIntent from Stripe
     * 
     * @param paymentIntentId Stripe PaymentIntent ID
     * @return PaymentIntent object
     * @throws StripeException if retrieval fails
     */
    public PaymentIntent retrievePaymentIntent(String paymentIntentId) throws StripeException {
        try {
            logger.info("Retrieving payment intent: {}", paymentIntentId);
            return PaymentIntent.retrieve(paymentIntentId);
        } catch (StripeException e) {
            logger.error("Failed to retrieve payment intent: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieve an existing PaymentMethod from Stripe
     * 
     * @param paymentMethodId Stripe PaymentMethod ID
     * @return PaymentMethod object
     * @throws StripeException if retrieval fails
     */
    public PaymentMethod retrievePaymentMethod(String paymentMethodId) throws StripeException {
        try {
            logger.info("Retrieving payment method: {}", paymentMethodId);
            return PaymentMethod.retrieve(paymentMethodId);
        } catch (StripeException e) {
            logger.error("Failed to retrieve payment method: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Check if a PaymentIntent was successful
     * 
     * @param paymentIntent The PaymentIntent to check
     * @return true if payment succeeded, false otherwise
     */
    public boolean isPaymentSuccessful(PaymentIntent paymentIntent) {
        return "succeeded".equals(paymentIntent.getStatus());
    }

    /**
     * Check if a PaymentIntent requires further action (e.g., 3D Secure)
     * 
     * @param paymentIntent The PaymentIntent to check
     * @return true if action required, false otherwise
     */
    public boolean requiresAction(PaymentIntent paymentIntent) {
        return "requires_action".equals(paymentIntent.getStatus());
    }

    /**
     * Get human-readable error message from StripeException
     * 
     * @param e StripeException
     * @return User-friendly error message
     */
    public String getErrorMessage(StripeException e) {
        if (e.getCode() != null) {
            return switch (e.getCode()) {
                case "card_declined" -> "Your card was declined. Please try another payment method.";
                case "expired_card" -> "Your card has expired. Please use a different card.";
                case "incorrect_cvc" -> "The card security code (CVC) is incorrect.";
                case "insufficient_funds" -> "Your card has insufficient funds.";
                case "invalid_expiry_month" -> "The expiration month is invalid.";
                case "invalid_expiry_year" -> "The expiration year is invalid.";
                case "invalid_number" -> "The card number is invalid.";
                case "processing_error" -> "An error occurred while processing your card. Please try again.";
                default -> "Payment failed: " + e.getMessage();
            };
        }
        return "Payment processing error: " + e.getMessage();
    }
}

