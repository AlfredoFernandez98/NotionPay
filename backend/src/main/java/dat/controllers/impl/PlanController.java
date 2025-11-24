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
            Long planId = Long.parseLong(ctx.pathParam("id"));
            Optional<Plan> plan = planDAO.getById(planId);

            if (plan.isEmpty()) {
                ctx.status(404);
                ctx.json("Plan not found");
                return;
            }

            ctx.status(200);
            ctx.json(convertToDTO(plan.get()));
        } catch (NumberFormatException e) {
            ctx.status(400);
            ctx.json("Invalid plan ID format");
        } catch (Exception e) {
            ctx.status(500);
            ctx.json("Error: " + e.getMessage());
        }
    }

    @Override
    public void readAll(Context ctx) {
        try {
            Set<Plan> plans = planDAO.getAll();
            Set<PlanDTO> planDTOs = plans.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toSet());

            ctx.status(200);
            ctx.json(planDTOs);
        } catch (Exception e) {
            ctx.status(500);
            ctx.json("Error: " + e.getMessage());
        }
    }

    /**
     * GET /api/plans/active
     * Get all active plans (for customer selection)
     */
    public void readAllActive(Context ctx) {
        try {
            Set<Plan> plans = planDAO.getAllActivePlans();
            Set<PlanDTO> planDTOs = plans.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toSet());

            ctx.status(200);
            ctx.json(planDTOs);
        } catch (Exception e) {
            ctx.status(500);
            ctx.json("Error: " + e.getMessage());
        }
    }

    @Override
    public void create(Context ctx) {
        try {
            PlanDTO planDTO = ctx.bodyAsClass(PlanDTO.class);

            // Validate input
            if (planDTO.name == null || planDTO.name.isEmpty()) {
                ctx.status(400);
                ctx.json("Plan name is required");
                return;
            }
            if (planDTO.priceCents == null || planDTO.priceCents < 0) {
                ctx.status(400);
                ctx.json("Plan price must be valid");
                return;
            }

            Plan plan = new Plan(
                    planDTO.name,
                    planDTO.period,
                    planDTO.priceCents,
                    planDTO.currency,
                    planDTO.description,
                    planDTO.active != null ? planDTO.active : true
            );

            Plan createdPlan = planDAO.create(plan);
            ctx.status(201);
            ctx.json(convertToDTO(createdPlan));
        } catch (Exception e) {
            ctx.status(500);
            ctx.json("Error: " + e.getMessage());
        }
    }

    @Override
    public void update(Context ctx) {
        try {
            Long planId = Long.parseLong(ctx.pathParam("id"));
            PlanDTO planDTO = ctx.bodyAsClass(PlanDTO.class);

            Optional<Plan> existingPlan = planDAO.getById(planId);
            if (existingPlan.isEmpty()) {
                ctx.status(404);
                ctx.json("Plan not found");
                return;
            }

            Plan plan = existingPlan.get();
            plan.setName(planDTO.name);
            plan.setPeriod(planDTO.period);
            plan.setPriceCents(planDTO.priceCents);
            plan.setCurrency(planDTO.currency);
            plan.setDescription(planDTO.description);
            plan.setActive(planDTO.active);

            planDAO.update(plan);
            ctx.status(200);
            ctx.json(convertToDTO(plan));
        } catch (NumberFormatException e) {
            ctx.status(400);
            ctx.json("Invalid plan ID format");
        } catch (Exception e) {
            ctx.status(500);
            ctx.json("Error: " + e.getMessage());
        }
    }

    @Override
    public void delete(Context ctx) {
        try {
            Long planId = Long.parseLong(ctx.pathParam("id"));

            Optional<Plan> plan = planDAO.getById(planId);
            if (plan.isEmpty()) {
                ctx.status(404);
                ctx.json("Plan not found");
                return;
            }

            planDAO.delete(planId);
            ctx.status(200);
            ctx.json("Plan deleted successfully");
        } catch (NumberFormatException e) {
            ctx.status(400);
            ctx.json("Invalid plan ID format");
        } catch (Exception e) {
            ctx.status(500);
            ctx.json("Error: " + e.getMessage());
        }
    }

    /**
     * Helper method to convert Plan entity to DTO
     */
    private PlanDTO convertToDTO(Plan plan) {
        PlanDTO dto = new PlanDTO();
        dto.id = plan.getId();
        dto.name = plan.getName();
        dto.period = plan.getPeriod();
        dto.priceCents = plan.getPriceCents();
        dto.currency = plan.getCurrency();
        dto.description = plan.getDescription();
        dto.active = plan.getActive();
        return dto;
    }
}
