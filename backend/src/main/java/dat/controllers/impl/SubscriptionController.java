package dat.controllers.impl;

import dat.controllers.IController;
import dat.dtos.SubscriptionDTO;
import dat.entities.Session;
import dat.entities.Subscription;
import dat.services.SessionService;
import dat.services.SubscriptionService;
import dat.utils.ErrorResponse;
import io.javalin.http.Context;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Controller for Subscription endpoints
 * 
 * ARCHITECTURE: This controller ONLY uses Services (no DAOs)
 * All business logic is delegated to the Service layer
 */
public class SubscriptionController implements IController<SubscriptionDTO> {
    
    // ✅ ONLY Services (no DAOs)
    private final SubscriptionService subscriptionService;
    private final SessionService sessionService;
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionController.class);
    
    public SubscriptionController(EntityManagerFactory emf) {
        this.subscriptionService = SubscriptionService.getInstance(emf);
        this.sessionService = SessionService.getInstance(emf);
    }

    /**
     * GET /api/subscriptions/{id}
     * Get subscription by ID
     */
    @Override
    public void read(Context ctx) {
        try {
            Long id = Long.parseLong(ctx.pathParam("id"));
            Optional<Subscription> subscription = subscriptionService.getById(id);
            
            if (subscription.isEmpty()) {
                ErrorResponse.notFound(ctx, "Subscription not found with ID: " + id);
                return;
            }
            
            SubscriptionDTO dto = convertToDto(subscription.get());
            ctx.status(200).json(dto);
            logger.info("Retrieved subscription ID: {}", id);
            
        } catch (NumberFormatException e) {
            ErrorResponse.badRequest(ctx, "Invalid subscription ID format");
        } catch (Exception e) {
            ErrorResponse.internalError(ctx, "Failed to retrieve subscription", logger, e);
        }
    }

    /**
     * GET /api/customers/{customerId}/subscription
     * Get active subscription for a customer
     */
    public void getCustomerSubscription(Context ctx) {
        try {
            Long customerId = Long.parseLong(ctx.pathParam("customerId"));
            Optional<Subscription> subscription = subscriptionService.getActiveSubscriptionForCustomer(customerId);
            
            if (subscription.isEmpty()) {
                ErrorResponse.notFound(ctx, "No active subscription found for customer ID: " + customerId);
                return;
            }
            
            SubscriptionDTO dto = convertToDto(subscription.get());
            ctx.status(200).json(dto);
            logger.info("Retrieved subscription for customer ID: {}", customerId);
            
        } catch (NumberFormatException e) {
            ErrorResponse.badRequest(ctx, "Invalid customer ID format");
        } catch (Exception e) {
            ErrorResponse.internalError(ctx, "Failed to retrieve subscription", logger, e);
        }
    }

    /**
     * PUT /api/subscriptions/{id}/cancel
     * Cancel a subscription
     */
    public void cancel(Context ctx) {
        try {
            Long id = Long.parseLong(ctx.pathParam("id"));
            
            // Get session for activity logging
            Session session = getSessionFromContext(ctx);
            
            // Delegate to service
            Subscription subscription = subscriptionService.cancelSubscription(id, session);
            
            SubscriptionDTO dto = convertToDto(subscription);
            ctx.status(200).json(dto);
            logger.info("Canceled subscription ID: {}", id);
            
        } catch (NumberFormatException e) {
            ErrorResponse.badRequest(ctx, "Invalid subscription ID format");
        } catch (SubscriptionService.SubscriptionServiceException e) {
            ErrorResponse.notFound(ctx, e.getMessage());
        } catch (Exception e) {
            ErrorResponse.internalError(ctx, "Failed to cancel subscription", logger, e);
        }
    }

    @Override
    public void readAll(Context ctx) {
        ErrorResponse.notImplemented(ctx, "Not implemented - use customer-specific endpoint");
    }

    @Override
    public void create(Context ctx) {
        ErrorResponse.notImplemented(ctx, "Subscriptions are created automatically during registration");
    }

    @Override
    public void update(Context ctx) {
        ErrorResponse.notImplemented(ctx, "Use cancel endpoint instead");
    }

    @Override
    public void delete(Context ctx) {
        ErrorResponse.notImplemented(ctx, "Subscriptions cannot be deleted, only canceled");
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
            String authHeader = ctx.header("Authorization");
            return sessionService.getFromAuthHeader(authHeader).orElse(null);
        } catch (Exception e) {
            logger.warn("Could not retrieve session from context: {}", e.getMessage());
            return null;
        }
    }
}
