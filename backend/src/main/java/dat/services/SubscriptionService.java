package dat.services;

import dat.daos.impl.ActivityLogDAO;
import dat.daos.impl.SubscriptionDAO;
import dat.entities.ActivityLog;
import dat.entities.Payment;
import dat.entities.Session;
import dat.entities.Subscription;
import dat.enums.ActivityLogStatus;
import dat.enums.ActivityLogType;
import dat.enums.Period;
import dat.enums.SubscriptionStatus;
import dat.utils.DateTimeUtil;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private final ActivityLogDAO activityLogDAO;


    public static SubscriptionService getInstance(EntityManagerFactory emf) {
        if (instance == null) {
            instance = new SubscriptionService(emf);
        }
        return instance;
    }

    private SubscriptionService(EntityManagerFactory emf) {
        this.subscriptionDAO = SubscriptionDAO.getInstance(emf);
        this.activityLogDAO = ActivityLogDAO.getInstance(emf);
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
            throw new IllegalStateException("Failed to update subscription after payment", e);
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
            throw new IllegalStateException("Failed to fetch subscriptions due for billing", e);
        }
    }

    /**
     * Get subscription by ID
     * 
     * @param subscriptionId The subscription ID
     * @return The subscription if found
     * @throws SubscriptionServiceException if subscription not found
     */
    public Subscription getSubscriptionById(Long subscriptionId) throws SubscriptionServiceException {
        return subscriptionDAO.getById(subscriptionId)
            .orElseThrow(() -> new SubscriptionServiceException("Subscription not found: " + subscriptionId));
    }

    /**
     * Get active subscription for a customer
     * 
     * @param customerId The customer ID
     * @return Optional containing the active subscription if found
     */
    public Optional<Subscription> getActiveSubscriptionForCustomer(Long customerId) {
        return subscriptionDAO.getActiveSubscriptionForCustomer(customerId);
    }

    /**
     * Get subscription by ID
     * 
     * @param id The subscription ID
     * @return Optional containing the subscription if found
     */
    public Optional<Subscription> getById(Long id) {
        return subscriptionDAO.getById(id);
    }

    /**
     * Cancel a subscription
     * 
     * @param subscriptionId The subscription ID to cancel
     * @param session Optional session for activity logging
     * @return The canceled subscription
     * @throws SubscriptionServiceException if cancellation fails
     */
    public Subscription cancelSubscription(Long subscriptionId, Session session) 
            throws SubscriptionServiceException {
        logger.info("Canceling subscription: {}", subscriptionId);
        
        try {
            Subscription subscription = subscriptionDAO.getById(subscriptionId)
                    .orElseThrow(() -> new SubscriptionServiceException("Subscription not found: " + subscriptionId));
            
            subscription.setStatus(SubscriptionStatus.CANCELED);
            subscription.setEndDate(DateTimeUtil.now());
            subscriptionDAO.update(subscription);
            
            // Log activity
            if (session != null) {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("subscriptionId", subscription.getId());
                metadata.put("planId", subscription.getPlan().getId());
                metadata.put("planName", subscription.getPlan().getName());
                metadata.put("canceledAt", subscription.getEndDate().toString());
                
                ActivityLog activityLog = new ActivityLog(
                    subscription.getCustomer(),
                    session,
                    ActivityLogType.SUBSCRIPTION_CANCELLED,
                    ActivityLogStatus.SUCCESS,
                    metadata
                );
                activityLogDAO.create(activityLog);
            }
            
            logger.info("Subscription canceled successfully: {}", subscriptionId);
            return subscription;
            
        } catch (Exception e) {
            logger.error("Error canceling subscription", e);
            throw new SubscriptionServiceException("Failed to cancel subscription: " + e.getMessage(), e);
        }
    }

    /**
     * Custom exception for subscription service operations
     */
    public static class SubscriptionServiceException extends Exception {
        public SubscriptionServiceException(String message) {
            super(message);
        }

        public SubscriptionServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
