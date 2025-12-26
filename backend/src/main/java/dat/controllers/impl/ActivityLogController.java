package dat.controllers.impl;

import dat.controllers.IController;
import dat.daos.impl.ActivityLogDAO;
import dat.dtos.ActivityLogDTO;
import dat.entities.ActivityLog;
import io.javalin.http.Context;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ActivityLogController implements IController<ActivityLogDTO> {
    private static final Logger logger = LoggerFactory.getLogger(ActivityLogController.class);
    private final ActivityLogDAO activityLogDAO;

    public ActivityLogController(EntityManagerFactory emf) {
        this.activityLogDAO = ActivityLogDAO.getInstance(emf);
    }

    @Override
    public void read(Context ctx) {
        try {
            Long id = Long.parseLong(ctx.pathParam("id"));
            ActivityLog activityLog = activityLogDAO.getById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Activity log not found"));
            
            ActivityLogDTO dto = convertToDTO(activityLog);
            ctx.status(200).json(dto);
            logger.info("Retrieved activity log ID: {}", id);
            
        } catch (NumberFormatException e) {
            ctx.status(400).json("{\"msg\": \"Invalid activity log ID format\"}");
        } catch (IllegalArgumentException e) {
            ctx.status(404).json("{\"msg\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("Error retrieving activity log", e);
            ctx.status(500).json("{\"msg\": \"Error retrieving activity log: " + e.getMessage() + "\"}");
        }
    }

    @Override
    public void readAll(Context ctx) {
        ctx.status(501).json("{\"msg\": \"Use customer-specific endpoint: GET /api/customers/{id}/activities\"}");
    }

    /**
     * GET /api/customers/{customerId}/activities
     * Get all activity logs for a customer
     */
    public void getCustomerActivities(Context ctx) {
        try {
            Long customerId = Long.parseLong(ctx.pathParam("customerId"));
            
            Set<ActivityLog> activities = activityLogDAO.getByCustomerId(customerId);
            
            // Convert to DTOs and sort by timestamp descending
            List<ActivityLogDTO> dtos = activities.stream()
                    .map(this::convertToDTO)
                    .sorted((a, b) -> b.timestamp.compareTo(a.timestamp))
                    .limit(20) // Limit to last 20 activities
                    .collect(Collectors.toList());
            
            ctx.status(200).json(dtos);
            logger.info("Retrieved {} activities for customer ID: {}", dtos.size(), customerId);
            
        } catch (NumberFormatException e) {
            ctx.status(400).json("{\"msg\": \"Invalid customer ID format\"}");
        } catch (Exception e) {
            logger.error("Error retrieving customer activities", e);
            ctx.status(500).json("{\"msg\": \"Error retrieving activities: " + e.getMessage() + "\"}");
        }
    }

    @Override
    public void create(Context ctx) {
        ctx.status(501).json("{\"msg\": \"Activity logs are created automatically by the system\"}");
    }

    @Override
    public void update(Context ctx) {
        ctx.status(501).json("{\"msg\": \"Activity logs cannot be updated\"}");
    }

    @Override
    public void delete(Context ctx) {
        ctx.status(501).json("{\"msg\": \"Activity logs cannot be deleted\"}");
    }

    private ActivityLogDTO convertToDTO(ActivityLog activityLog) {
        ActivityLogDTO dto = new ActivityLogDTO();
        dto.id = activityLog.getId();
        dto.customerId = activityLog.getCustomer().getId();
        dto.sessionId = activityLog.getSession() != null ? activityLog.getSession().getId() : null;
        dto.type = activityLog.getType();
        dto.status = activityLog.getStatus();
        dto.timestamp = activityLog.getTimestamp();
        dto.metadata = activityLog.getMetadata();
        return dto;
    }
}

