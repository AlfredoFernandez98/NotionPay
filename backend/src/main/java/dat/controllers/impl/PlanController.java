package dat.controllers.impl;

import dat.controllers.IController;
import dat.daos.impl.PlanDAO;
import dat.dtos.PlanDTO;
import dat.entities.Plan;
import io.javalin.http.Context;
import jakarta.persistence.EntityManagerFactory;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class PlanController implements IController<PlanDTO> {
    private final PlanDAO planDAO;

    public PlanController(EntityManagerFactory emf) {
        this.planDAO = PlanDAO.getInstance(emf);
    }

    @Override
    public void read(Context ctx) {
        try {
            // Get ID from path parameter
            Long id = Long.parseLong(ctx.pathParam("id"));
            
            // Get plan from DAO
            Optional<Plan> planOpt = planDAO.getById(id);
            
            if (planOpt.isPresent()) {
                // Convert entity → DTO
                PlanDTO dto = convertToDto(planOpt.get());
                ctx.status(200);
                ctx.json(dto);
            } else {
                ctx.status(404);
                ctx.json("Plan not found");
            }
            
        } catch (NumberFormatException e) {
            ctx.status(400);
            ctx.json("Invalid plan ID format");
        } catch (Exception e) {
            ctx.status(500);
            ctx.json("Error fetching plan: " + e.getMessage());
        }
    }

    @Override
    public void readAll(Context ctx) {
        try {
            // Get all ACTIVE plans only
            Set<Plan> plans = planDAO.getAllActivePlans();
            
            // Convert Set<Plan> → Set<PlanDTO>
            Set<PlanDTO> plansDTO = plans.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toSet());
            
            ctx.status(200);
            ctx.json(plansDTO);
            
        } catch (Exception e) {
            ctx.status(500);
            ctx.json("Error fetching plans: " + e.getMessage());
        }
    }

    @Override
    public void create(Context ctx) {

    }

    @Override
    public void update(Context ctx) {

    }

    @Override
    public void delete(Context ctx) {

    }
    /**
     * Helper: Convert Plan entity to PlanDTO
     */
    private PlanDTO convertToDto(Plan entity) {
        PlanDTO dto = new PlanDTO();
        dto.id = entity.getId();
        dto.name = entity.getName();
        dto.period = entity.getPeriod();
        dto.priceCents = entity.getPriceCents();
        dto.currency = entity.getCurrency();
        dto.description = entity.getDescription();
        dto.active = entity.getActive();
        return dto;
    }
}
