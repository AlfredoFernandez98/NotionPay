package dat.services;

import dat.daos.impl.PlanDAO;
import dat.entities.Plan;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Set;

/**
 * Service for managing subscription plans
 * Handles plan retrieval operations
 * 
 * @author NotionPay Team
 */
public class PlanService {
    private static PlanService instance;
    private static final Logger logger = LoggerFactory.getLogger(PlanService.class);
    
    private final PlanDAO planDAO;

    public static PlanService getInstance(EntityManagerFactory emf) {
        if (instance == null) {
            instance = new PlanService(emf);
        }
        return instance;
    }

    private PlanService(EntityManagerFactory emf) {
        this.planDAO = PlanDAO.getInstance(emf);
        logger.info("PlanService initialized");
    }

    /**
     * Get plan by ID
     */
    public Optional<Plan> getById(Long id) {
        return planDAO.getById(id);
    }

    /**
     * Get all active plans
     */
    public Set<Plan> getAllActivePlans() {
        return planDAO.getAllActivePlans();
    }
}
