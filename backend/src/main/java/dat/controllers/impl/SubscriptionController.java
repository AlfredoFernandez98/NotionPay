package dat.controllers.impl;

import dat.controllers.IController;
import dat.daos.impl.ActivityLogDAO;
import dat.daos.impl.CustomerDAO;
import dat.daos.impl.PlanDAO;
import dat.daos.impl.SubscriptionDAO;
import dat.dtos.SubscriptionDTO;
import dat.entities.ActivityLog;
import dat.entities.Customer;
import dat.entities.Plan;
import dat.entities.Subscription;
import dat.enums.ActivityLogStatus;
import dat.enums.ActivityLogType;
import dat.enums.AnchorPolicy;
import dat.enums.SubscriptionStatus;
import io.javalin.http.Context;
import jakarta.persistence.EntityManagerFactory;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class SubscriptionController implements IController<SubscriptionDTO> {
    private final SubscriptionDAO subscriptionDAO;
    private final PlanDAO planDAO;
    private final CustomerDAO customerDAO;
    private final ActivityLogDAO activityLogDAO;

    public SubscriptionController(EntityManagerFactory emf) {
        this.subscriptionDAO=SubscriptionDAO.getInstance(emf);
        this.planDAO=PlanDAO.getInstance(emf);
        this.customerDAO=CustomerDAO.getInstance(emf);
        this.activityLogDAO=ActivityLogDAO.getInstance(emf);

    }

    @Override
    public void read(Context ctx) {

        try{
            Long subscriptionId=Long.parseLong(ctx.pathParam("id"));

            Optional<Subscription> subscription=subscriptionDAO.getById(subscriptionId);

            if(subscription.isEmpty()){
                ctx.status(404);
                ctx.json("Subscription not found");
                return;
            }
            ctx.status(200);
            ctx.json(convertToDTO(subscription.get()));

        } catch (NumberFormatException e) {
            ctx.status(400);
            ctx.json("Invalid subscription id");
        }catch (Exception e){
            ctx.status(500);
            ctx.json("Error: " + e.getMessage());
        }


    }

    @Override
    public void readAll(Context ctx) {
        try {
            Set<Subscription> subscriptions = subscriptionDAO.getAll();
            ctx.status(200);
            ctx.json(subscriptions.stream().map(this::convertToDTO).toList());
        } catch (Exception e) {
            ctx.status(500);
            ctx.json("Error: " + e.getMessage());
        }
    }

    /**
     * POST /api/subscriptions
     * Customer selects a plan and creates a subscription
     * 
     * Request body:
     * {
     *   "customerId": 1,
     *   "planId": 2
     * }
     */
    @Override
    public void create(Context ctx) {
        try {
            // ===== 1. GET DATA FROM REQUEST =====
            SubscriptionDTO subscriptionDTO = ctx.bodyAsClass(SubscriptionDTO.class);

            // ===== 2. VALIDATE INPUTS =====
            if (subscriptionDTO.customerId == null) {
                ctx.status(400);
                ctx.json("Customer ID is required");
                return;
            }
            if (subscriptionDTO.planId == null) {
                ctx.status(400);
                ctx.json("Plan ID is required");
                return;
            }

            // ===== 3. FETCH CUSTOMER FROM DATABASE =====
            Optional<Customer> customerOpt = customerDAO.getById(subscriptionDTO.customerId);
            if (customerOpt.isEmpty()) {
                ctx.status(404);
                ctx.json("Customer not found with ID: " + subscriptionDTO.customerId);
                return;
            }
            Customer customer = customerOpt.get();

            // ===== 4. FETCH PLAN FROM DATABASE =====
            Optional<Plan> planOpt = planDAO.getById(subscriptionDTO.planId);
            if (planOpt.isEmpty()) {
                ctx.status(404);
                ctx.json("Plan not found with ID: " + subscriptionDTO.planId);
                return;
            }
            Plan plan = planOpt.get();

            // ===== 5. CHECK IF CUSTOMER ALREADY HAS ACTIVE SUBSCRIPTION =====
            Optional<Subscription> activeSubOpt = subscriptionDAO.getActiveSubscriptionForCustomer(customer.getId());
            if (activeSubOpt.isPresent()) {
                ctx.status(409);
                ctx.json("Customer already has an active subscription");
                return;
            }

            // ===== 6. CREATE NEW SUBSCRIPTION =====
            Subscription subscription = new Subscription(
                    customer,
                    plan,
                    SubscriptionStatus.ACTIVE,
                    OffsetDateTime.now(),                      // Start now
                    OffsetDateTime.now().plusMonths(1),        // Next billing in 1 month
                    AnchorPolicy.ANNIVERSARY                    // Bill on anniversary date
            );

            // ===== 7. SAVE TO DATABASE =====
            Subscription createdSubscription = subscriptionDAO.create(subscription);

            // ===== 8. LOG THE ACTIVITY =====
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("planId", plan.getId());
            metadata.put("planName", plan.getName());
            metadata.put("price", plan.getPriceCents());

            ActivityLog log = new ActivityLog(
                    customer,
                    null,  // Session would be passed from auth context if available
                    ActivityLogType.PAYMENT,
                    ActivityLogStatus.SUCCESS,
                    metadata
            );
            activityLogDAO.create(log);

            // ===== 9. RETURN SUCCESS RESPONSE =====
            ctx.status(201);
            ctx.json(convertToDTO(createdSubscription));

        } catch (Exception e) {
            ctx.status(500);
            ctx.json("Error creating subscription: " + e.getMessage());
        }
    }

    /**
     * PUT /api/subscriptions/{id}
     * Update subscription (e.g., change status, update billing date)
     */
    @Override
    public void update(Context ctx) {
        try {
            Long subscriptionId = Long.parseLong(ctx.pathParam("id"));
            SubscriptionDTO subscriptionDTO = ctx.bodyAsClass(SubscriptionDTO.class);

            Optional<Subscription> subscriptionOpt = subscriptionDAO.getById(subscriptionId);
            if (subscriptionOpt.isEmpty()) {
                ctx.status(404);
                ctx.json("Subscription not found");
                return;
            }

            Subscription subscription = subscriptionOpt.get();

            // Update fields if provided
            if (subscriptionDTO.status != null) {
                subscription.setStatus(subscriptionDTO.status);
            }
            if (subscriptionDTO.nextBillingDate != null) {
                subscription.setNextBillingDate(subscriptionDTO.nextBillingDate);
            }

            subscriptionDAO.update(subscription);

            ctx.status(200);
            ctx.json(convertToDTO(subscription));
        } catch (NumberFormatException e) {
            ctx.status(400);
            ctx.json("Invalid subscription ID format");
        } catch (Exception e) {
            ctx.status(500);
            ctx.json("Error: " + e.getMessage());
        }
    }

    /**
     * DELETE /api/subscriptions/{id}
     * Cancel subscription
     */
    @Override
    public void delete(Context ctx) {
        try {
            Long subscriptionId = Long.parseLong(ctx.pathParam("id"));

            Optional<Subscription> subscriptionOpt = subscriptionDAO.getById(subscriptionId);
            if (subscriptionOpt.isEmpty()) {
                ctx.status(404);
                ctx.json("Subscription not found");
                return;
            }

            Subscription subscription = subscriptionOpt.get();
            Customer customer = subscription.getCustomer();

            // ===== CANCEL SUBSCRIPTION =====
            subscription.setStatus(SubscriptionStatus.CANCELED);
            subscription.setEndDate(OffsetDateTime.now());
            subscriptionDAO.update(subscription);

            // ===== LOG CANCELLATION =====
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("subscriptionId", subscription.getId());
            metadata.put("planName", subscription.getPlan().getName());
            metadata.put("reason", "Customer cancelled");

            ActivityLog log = new ActivityLog(
                    customer,
                    null,  // Session would be passed from context if available
                    ActivityLogType.LOGOUT,  // Using as general action log
                    ActivityLogStatus.SUCCESS,
                    metadata
            );
            activityLogDAO.create(log);

            ctx.status(200);
            ctx.json("Subscription cancelled successfully");

        } catch (NumberFormatException e) {
            ctx.status(400);
            ctx.json("Invalid subscription ID format");
        } catch (Exception e) {
            ctx.status(500);
            ctx.json("Error: " + e.getMessage());
        }
    }

    /**
     * Helper method to convert Subscription entity to DTO
     */
    private SubscriptionDTO convertToDTO(Subscription subscription) {
        SubscriptionDTO dto = new SubscriptionDTO();
        dto.id = subscription.getId();
        dto.customerId = subscription.getCustomer().getId();
        dto.planId = subscription.getPlan().getId();
        dto.planName = subscription.getPlan().getName();
        dto.status = subscription.getStatus();
        dto.startDate = subscription.getStartDate();
        dto.endDate = subscription.getEndDate();
        dto.nextBillingDate = subscription.getNextBillingDate();
        dto.anchorPolicy = subscription.getAnchorPolicy();
        return dto;
    }
}
