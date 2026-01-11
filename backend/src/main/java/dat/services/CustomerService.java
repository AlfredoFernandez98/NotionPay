package dat.services;

import dat.daos.impl.CustomerDAO;
import dat.daos.impl.SmsBalanceDAO;
import dat.daos.impl.SubscriptionDAO;
import dat.entities.Customer;
import dat.entities.Plan;
import dat.entities.SmsBalance;
import dat.entities.Subscription;
import dat.enums.AnchorPolicy;
import dat.enums.SubscriptionStatus;
import dat.security.daos.ISecurityDAO;
import dat.security.daos.SecurityDAO;
import dat.security.entities.User;
import dat.utils.DateTimeUtil;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Service for managing customers
 * Handles customer creation, retrieval, and related operations
 * 
 * @author NotionPay Team
 */
public class CustomerService {
    private static CustomerService instance;
    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);
    
    private final CustomerDAO customerDAO;
    private final SmsBalanceDAO smsBalanceDAO;
    private final SubscriptionDAO subscriptionDAO;
    private final SerialLinkVerificationService serialLinkService;
    private final ISecurityDAO securityDAO;

    public static CustomerService getInstance(EntityManagerFactory emf) {
        if (instance == null) {
            instance = new CustomerService(emf);
        }
        return instance;
    }

    private CustomerService(EntityManagerFactory emf) {
        this.customerDAO = CustomerDAO.getInstance(emf);
        this.smsBalanceDAO = SmsBalanceDAO.getInstance(emf);
        this.subscriptionDAO = SubscriptionDAO.getInstance(emf);
        this.serialLinkService = SerialLinkVerificationService.getInstance(emf);
        this.securityDAO = new SecurityDAO(emf);
        logger.info("CustomerService initialized");
    }

    /**
     * Get customer by ID
     */
    public Optional<Customer> getById(Long id) {
        return customerDAO.getById(id);
    }

    /**
     * Get customer by user email
     */
    public Optional<Customer> getByUserEmail(String email) {
        return customerDAO.getByUserEmail(email);
    }

    /**
     * Get SMS balance for a customer
     */
    public Optional<SmsBalance> getSmsBalance(Long customerId) throws CustomerServiceException {
        Customer customer = customerDAO.getById(customerId)
                .orElseThrow(() -> new CustomerServiceException("Customer not found: " + customerId));
        
        return smsBalanceDAO.getByExternalCustomerId(customer.getExternalCustomerId());
    }

    /**
     * Create a new customer with subscription and SMS balance
     * This is a complex operation that creates multiple related entities
     * 
     * @param email User email
     * @param companyName Company name
     * @param serialNumber Serial number
     * @return The created customer
     * @throws CustomerServiceException if creation fails
     */
    public Customer createCustomer(String email, String companyName, Integer serialNumber) 
            throws CustomerServiceException {
        logger.info("Creating customer with email: {}, serial: {}", email, serialNumber);
        
        try {
            // Validate serial number and email
            boolean isValid = serialLinkService.verifySerialNumberAndEmail(serialNumber, email);
            if (!isValid) {
                throw new CustomerServiceException("Invalid serial number or email combination");
            }

            // Get the Plan for this serial number
            Plan plan = serialLinkService.getPlanForSerialNumber(serialNumber);
            if (plan == null) {
                throw new CustomerServiceException("No plan found for serial number: " + serialNumber);
            }

            // Get SerialLink to fetch external_customer_id
            dat.entities.SerialLink serialLink = serialLinkService.getSerialLink(serialNumber);
            if (serialLink == null) {
                throw new CustomerServiceException("SerialLink not found for serial number: " + serialNumber);
            }

            // Get User
            User user = securityDAO.getUserByEmail(email);
            if (user == null) {
                throw new CustomerServiceException("No user found with email: " + email);
            }

            // Check if customer already exists
            if (customerDAO.getByUserEmail(email).isPresent()) {
                throw new CustomerServiceException("Customer already exists with email: " + email);
            }

            // Create Customer
            Customer customer = new Customer(
                user, 
                companyName, 
                serialNumber, 
                serialLink.getExternalCustomerId(),
                DateTimeUtil.now()
            );
            Customer savedCustomer = customerDAO.create(customer);
            
            // Create Subscription (using data from external system via SerialLink)
            Subscription subscription = new Subscription(
                savedCustomer,
                plan,
                SubscriptionStatus.ACTIVE,  // Already subscribed in external system
                DateTimeUtil.now(),
                serialLink.getNextPaymentDate(),  // From external system
                AnchorPolicy.ANNIVERSARY
            );
            subscriptionDAO.create(subscription);
            
            // Create SmsBalance (from external SMS provider)
            SmsBalance smsBalance = new SmsBalance(
                savedCustomer.getExternalCustomerId(), 
                serialLink.getInitialSmsBalance()
            );
            smsBalanceDAO.create(smsBalance);
            
            logger.info("Customer created successfully: ID {}", savedCustomer.getId());
            return savedCustomer;
            
        } catch (Exception e) {
            logger.error("Error creating customer", e);
            throw new CustomerServiceException("Failed to create customer: " + e.getMessage(), e);
        }
    }

    /**
     * Custom exception for customer service operations
     */
    public static class CustomerServiceException extends Exception {
        public CustomerServiceException(String message) {
            super(message);
        }

        public CustomerServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
