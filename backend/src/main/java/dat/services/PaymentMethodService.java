package dat.services;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentMethod;
import dat.daos.impl.ActivityLogDAO;
import dat.daos.impl.CustomerDAO;
import dat.daos.impl.PaymentMethodDAO;
import dat.entities.ActivityLog;
import dat.entities.Customer;
import dat.entities.Session;
import dat.enums.ActivityLogStatus;
import dat.enums.ActivityLogType;
import dat.enums.PaymentMethodStatus;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Service for managing payment methods
 * Handles payment method creation, retrieval, and Stripe integration
 * 
 * @author NotionPay Team
 */
public class PaymentMethodService {
    private static PaymentMethodService instance;
    private static final Logger logger = LoggerFactory.getLogger(PaymentMethodService.class);
    
    private final PaymentMethodDAO paymentMethodDAO;
    private final CustomerDAO customerDAO;
    private final ActivityLogDAO activityLogDAO;
    private final StripePaymentService stripeService;

    public static PaymentMethodService getInstance(EntityManagerFactory emf) {
        if (instance == null) {
            instance = new PaymentMethodService(emf);
        }
        return instance;
    }

    private PaymentMethodService(EntityManagerFactory emf) {
        this.paymentMethodDAO = PaymentMethodDAO.getInstance(emf);
        this.customerDAO = CustomerDAO.getInstance(emf);
        this.activityLogDAO = ActivityLogDAO.getInstance(emf);
        this.stripeService = StripePaymentService.getInstance();
        logger.info("PaymentMethodService initialized");
    }

    /**
     * Add a new payment method for a customer
     * 
     * @param customerId Customer ID
     * @param cardNumber Card number
     * @param expMonth Expiration month
     * @param expYear Expiration year
     * @param cvc CVC code
     * @param isDefault Whether this should be the default payment method
     * @param session Optional session for activity logging
     * @return The created payment method entity
     * @throws PaymentMethodException if creation fails
     */
    public dat.entities.PaymentMethod addPaymentMethod(
            Long customerId,
            String cardNumber,
            Long expMonth,
            Long expYear,
            String cvc,
            boolean isDefault,
            Session session
    ) throws PaymentMethodException {
        logger.info("Adding payment method for customer: {}", customerId);
        
        try {
            // Get customer
            Customer customer = customerDAO.getById(customerId)
                    .orElseThrow(() -> new PaymentMethodException("Customer not found: " + customerId));

            // Create payment method in Stripe
            PaymentMethod stripePaymentMethod = stripeService.createPaymentMethod(
                    cardNumber, expMonth, expYear, cvc
            );

            // Check for duplicate card using fingerprint
            String fingerprint = stripePaymentMethod.getCard().getFingerprint();
            Optional<dat.entities.PaymentMethod> existingPaymentMethod = 
                    paymentMethodDAO.findByFingerprint(customer, fingerprint);
            
            if (existingPaymentMethod.isPresent()) {
                logger.warn("Duplicate card detected for customer {}: fingerprint {}", customerId, fingerprint);
                throw new PaymentMethodException("This card has already been added to your account");
            }

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
                    PaymentMethodStatus.ACTIVE,
                    stripePaymentMethod.getCard().getFingerprint()
            );

            dat.entities.PaymentMethod savedPaymentMethod = paymentMethodDAO.create(paymentMethod);
            logger.info("Payment method added successfully: ID {}", savedPaymentMethod.getId());

            // Log activity
            if (session != null) {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("paymentMethodId", savedPaymentMethod.getId());
                metadata.put("brand", savedPaymentMethod.getBrand());
                metadata.put("last4", savedPaymentMethod.getLast4());
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

            return savedPaymentMethod;
            
        } catch (StripeException e) {
            logger.error("Stripe error while adding payment method: {}", e.getMessage());
            throw new PaymentMethodException("Stripe error: " + stripeService.getErrorMessage(e), e);
        } catch (Exception e) {
            logger.error("Error adding payment method", e);
            throw new PaymentMethodException("Failed to add payment method: " + e.getMessage(), e);
        }
    }

    /**
     * Get payment method by ID
     */
    public Optional<dat.entities.PaymentMethod> getById(Long id) {
        return paymentMethodDAO.getById(id);
    }

    /**
     * Get all payment methods for a customer
     */
    public Set<dat.entities.PaymentMethod> getByCustomer(Long customerId) throws PaymentMethodException {
        Customer customer = customerDAO.getById(customerId)
                .orElseThrow(() -> new PaymentMethodException("Customer not found: " + customerId));
        return paymentMethodDAO.getByCustomer(customer);
    }

    /**
     * Custom exception for payment method operations
     */
    public static class PaymentMethodException extends Exception {
        public PaymentMethodException(String message) {
            super(message);
        }

        public PaymentMethodException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
