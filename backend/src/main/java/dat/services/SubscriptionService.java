package dat.services;

import dat.daos.impl.SubscriptionDAO;
import dat.entities.Payment;
import dat.entities.Subscription;
import dat.enums.Period;
import dat.enums.SubscriptionStatus;
import dat.utils.DateTimeUtil;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for handling subscription billing logic
 * Manages subscription lifecycle, billing date calculations, and renewals
 * 
 * @author NotionPay Team
 */
public class SubscriptionService {
    private static SubscriptionService instance;
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionService.class);
    
    private final SubscriptionDAO subscriptionDAO;


    public static SubscriptionService getInstance(EntityManagerFactory emf) {
        if (instance == null) {
            instance = new SubscriptionService(emf);
        }
        return instance;
    }

    private SubscriptionService(EntityManagerFactory emf) {
        this.subscriptionDAO = SubscriptionDAO.getInstance(emf);
        logger.info("SubscriptionService initialized");
    }

    /**
     * Calculate the next billing date based on subscription's plan period
     * Handles edge cases like month-end dates and leap years
     * 
     * @param subscription The subscription to calculate next billing date for
     * @return The next billing date
     */
    public OffsetDateTime calculateNextBillingDate(Subscription subscription) {
        if (subscription == null || subscription.getNextBillingDate() == null) {
            throw new IllegalArgumentException("Subscription and nextBillingDate cannot be null");
        }

        OffsetDateTime currentBillingDate = subscription.getNextBillingDate();
        Period period = subscription.getPlan().getPeriod();
        
        logger.debug("Calculating next billing date from {} for period {}", currentBillingDate, period);

        OffsetDateTime nextDate;
        
        switch (period) {
            case MONTHLY:
                // Add one month, handling edge cases
                nextDate = addMonthSafely(currentBillingDate);
                break;
                
            case YEARLY:
                // Add one year, handling leap year edge cases
                nextDate = addYearSafely(currentBillingDate);
                break;
                
            default:
                throw new IllegalStateException("Unknown period type: " + period);
        }
        
        logger.debug("Next billing date calculated: {}", nextDate);
        return nextDate;
    }

    /**
     * Add one month to a date, handling edge cases
     * Example: Jan 31 → Feb 28/29, May 31 → Jun 30
     * 
     * @param date The date to add a month to
     * @return The date plus one month
     */
    private OffsetDateTime addMonthSafely(OffsetDateTime date) {
        OffsetDateTime nextMonth = date.plusMonths(1);
        
        // Handle case where original day doesn't exist in next month
        // Example: Jan 31 → Feb 31 (doesn't exist) → Feb 28/29
        if (date.getDayOfMonth() > nextMonth.getDayOfMonth()) {
            // Set to last day of the month
            nextMonth = nextMonth.with(TemporalAdjusters.lastDayOfMonth());
            logger.debug("Adjusted to last day of month: {}", nextMonth);
        }
        
        return nextMonth;
    }

    /**
     * Add one year to a date, handling leap year edge cases
     * Example: Feb 29, 2024 → Feb 28, 2025
     * 
     * @param date The date to add a year to
     * @return The date plus one year
     */
    private OffsetDateTime addYearSafely(OffsetDateTime date) {
        OffsetDateTime nextYear = date.plusYears(1);
        
        // Handle leap year case: Feb 29 → Feb 28 in non-leap year
        if (date.getMonthValue() == 2 && date.getDayOfMonth() == 29) {
            if (nextYear.getDayOfMonth() != 29) {
                nextYear = nextYear.with(TemporalAdjusters.lastDayOfMonth());
                logger.debug("Adjusted leap year date to: {}", nextYear);
            }
        }
        
        return nextYear;
    }

    /**
     * Update subscription after a successful payment
     * Calculates and sets the new nextBillingDate
     * 
     * @param subscription The subscription that was paid
     * @param payment The payment that was made (for logging/audit purposes)
     */
    public void updateSubscriptionAfterPayment(Subscription subscription, Payment payment) {
        if (subscription == null) {
            throw new IllegalArgumentException("Subscription cannot be null");
        }
        
        logger.info("Updating subscription {} after payment {}", 
            subscription.getId(), payment != null ? payment.getId() : "null");

        try {
            // Calculate new billing date
            OffsetDateTime oldBillingDate = subscription.getNextBillingDate();
            OffsetDateTime newBillingDate = calculateNextBillingDate(subscription);
            
            // Update subscription
            subscription.setNextBillingDate(newBillingDate);
            
            // Ensure subscription is active
            if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
                subscription.setStatus(SubscriptionStatus.ACTIVE);
                logger.info("Subscription {} status changed to ACTIVE", subscription.getId());
            }
            
            // Persist changes
            subscriptionDAO.update(subscription);
            
            logger.info("Subscription {} updated: nextBillingDate changed from {} to {}", 
                subscription.getId(), oldBillingDate, newBillingDate);
                
        } catch (Exception e) {
            logger.error("Failed to update subscription {} after payment", subscription.getId(), e);
            throw new RuntimeException("Failed to update subscription after payment", e);
        }
    }

    /**
     * Get all subscriptions that are due for billing
     * Returns subscriptions where nextBillingDate is today or earlier and status is ACTIVE
     * 
     * @return List of subscriptions due for billing
     */
    public List<Subscription> getSubscriptionsDueForBilling() {
        logger.info("Fetching subscriptions due for billing");
        
        try {
            OffsetDateTime now = DateTimeUtil.now();
            
            // Get all active subscriptions
            List<Subscription> dueSubscriptions = subscriptionDAO.getAll().stream()
                .filter(sub -> sub.getStatus() == SubscriptionStatus.ACTIVE)
                .filter(sub -> sub.getNextBillingDate() != null)
                .filter(sub -> sub.getNextBillingDate().isBefore(now) || 
                              sub.getNextBillingDate().isEqual(now))
                .collect(Collectors.toList());
            
            logger.info("Found {} subscriptions due for billing", dueSubscriptions.size());
            return dueSubscriptions;
            
        } catch (Exception e) {
            logger.error("Failed to fetch subscriptions due for billing", e);
            throw new RuntimeException("Failed to fetch subscriptions due for billing", e);
        }
    }

    /**
     * Get subscription by ID
     * 
     * @param subscriptionId The subscription ID
     * @return The subscription if found
     */
    public Subscription getSubscriptionById(Long subscriptionId) {
        return subscriptionDAO.getById(subscriptionId)
            .orElseThrow(() -> new IllegalArgumentException("Subscription not found: " + subscriptionId));
    }

    /**
     * Get active subscription for a customer
     * 
     * @param customerId The customer ID
     * @return The active subscription if found
     */
    public Subscription getActiveSubscriptionForCustomer(Long customerId) {
        return subscriptionDAO.getActiveSubscriptionForCustomer(customerId)
            .orElseThrow(() -> new IllegalArgumentException("No active subscription found for customer: " + customerId));
    }
}
