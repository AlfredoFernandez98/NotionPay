package dat.controllers.impl;

import dat.controllers.IController;
import dat.daos.impl.ActivityLogDAO;
import dat.daos.impl.SessionDAO;
import dat.daos.impl.SubscriptionDAO;
import dat.dtos.SubscriptionDTO;
import dat.entities.ActivityLog;
import dat.entities.Session;
import dat.entities.Subscription;
import dat.enums.ActivityLogStatus;
import dat.enums.ActivityLogType;
import dat.enums.SubscriptionStatus;
import io.javalin.http.Context;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SubscriptionController implements IController<SubscriptionDTO> {
    
    private final SubscriptionDAO subscriptionDAO;
    private final ActivityLogDAO activityLogDAO;
    private final SessionDAO sessionDAO;
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionController.class);
    
    public SubscriptionController(EntityManagerFactory emf) {
        this.subscriptionDAO = SubscriptionDAO.getInstance(emf);
        this.activityLogDAO = ActivityLogDAO.getInstance(emf);
        this.sessionDAO = SessionDAO.getInstance(emf);
    }

    /**
     * GET /api/subscriptions/{id}
     * Get subscription by ID
     */
    @Override
    public void read(Context ctx) {
        try {
            Long id = Long.parseLong(ctx.pathParam("id"));
            Optional<Subscription> subscription = subscriptionDAO.getById(id);
            
            if (subscription.isEmpty()) {
                ctx.status(404).json("{\"msg\": \"Subscription not found with ID: " + id + "\"}");
                return;
            }
            
            SubscriptionDTO dto = convertToDto(subscription.get());
            ctx.status(200).json(dto);
            logger.info("Retrieved subscription ID: {}", id);
            
        } catch (NumberFormatException e) {
            ctx.status(400).json("{\"msg\": \"Invalid subscription ID format\"}");
        } catch (Exception e) {
            logger.error("Error retrieving subscription: ", e);
            ctx.status(500).json("{\"msg\": \"Failed to retrieve subscription: " + e.getMessage() + "\"}");
        }
    }

    /**
     * GET /api/customers/{customerId}/subscription
     * Get active subscription for a customer
     */
    public void getCustomerSubscription(Context ctx) {
        try {
            Long customerId = Long.parseLong(ctx.pathParam("customerId"));
            Optional<Subscription> subscription = subscriptionDAO.getActiveSubscriptionForCustomer(customerId);
            
            if (subscription.isEmpty()) {
                ctx.status(404).json("{\"msg\": \"No active subscription found for customer ID: " + customerId + "\"}");
                return;
            }
            
            SubscriptionDTO dto = convertToDto(subscription.get());
            ctx.status(200).json(dto);
            logger.info("Retrieved subscription for customer ID: {}", customerId);
            
        } catch (NumberFormatException e) {
            ctx.status(400).json("{\"msg\": \"Invalid customer ID format\"}");
        } catch (Exception e) {
            logger.error("Error retrieving customer subscription: ", e);
            ctx.status(500).json("{\"msg\": \"Failed to retrieve subscription: " + e.getMessage() + "\"}");
        }
    }

    /**
     * PUT /api/subscriptions/{id}/cancel
     * Cancel a subscription
     */
    public void cancel(Context ctx) {
        try {
            Long id = Long.parseLong(ctx.pathParam("id"));
            Optional<Subscription> subscriptionOpt = subscriptionDAO.getById(id);
            
            if (subscriptionOpt.isEmpty()) {
                ctx.status(404).json("{\"msg\": \"Subscription not found with ID: " + id + "\"}");
                return;
            }
            
            Subscription subscription = subscriptionOpt.get();
            subscription.setStatus(SubscriptionStatus.CANCELED);
            subscription.setEndDate(java.time.OffsetDateTime.now());
            subscriptionDAO.update(subscription);
            
            // Log activity
            Session session = getSessionFromContext(ctx);
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
            
            SubscriptionDTO dto = convertToDto(subscription);
            ctx.status(200).json(dto);
            logger.info("Canceled subscription ID: {}", id);
            
        } catch (NumberFormatException e) {
            ctx.status(400).json("{\"msg\": \"Invalid subscription ID format\"}");
        } catch (Exception e) {
            logger.error("Error canceling subscription: ", e);
            ctx.status(500).json("{\"msg\": \"Failed to cancel subscription: " + e.getMessage() + "\"}");
        }
    }

    @Override
    public void readAll(Context ctx) {
        ctx.status(501).json("{\"msg\": \"Not implemented - use customer-specific endpoint\"}");
    }

    @Override
    public void create(Context ctx) {
        ctx.status(501).json("{\"msg\": \"Subscriptions are created automatically during registration\"}");
    }

    @Override
    public void update(Context ctx) {
        ctx.status(501).json("{\"msg\": \"Use cancel endpoint instead\"}");
    }

    @Override
    public void delete(Context ctx) {
        ctx.status(501).json("{\"msg\": \"Subscriptions cannot be deleted, only canceled\"}");
    }

    /**
     * Convert Subscription entity to DTO
     */
    private SubscriptionDTO convertToDto(Subscription subscription) {
        SubscriptionDTO dto = new SubscriptionDTO();
        dto.id = subscription.getId();
        dto.customerId = subscription.getCustomer().getId();
        dto.customerEmail = subscription.getCustomer().getUser().getEmail();
        dto.planId = subscription.getPlan().getId();
        dto.planName = subscription.getPlan().getName();
        dto.status = subscription.getStatus();
        dto.startDate = subscription.getStartDate();
        dto.endDate = subscription.getEndDate();
        dto.nextBillingDate = subscription.getNextBillingDate();
        dto.anchorPolicy = subscription.getAnchorPolicy();
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
}
