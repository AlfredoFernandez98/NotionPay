package dat.daos.impl;

import dat.daos.IDAO;
import dat.entities.ActivityLog;
import jakarta.persistence.EntityManagerFactory;

import java.util.Optional;
import java.util.Set;

/**
 * DAO for ActivityLog entity
 * TODO: Implement IDAO interface methods
 * TODO: Add custom methods like getByCustomerId, getByType, getByDateRange
 */
public class ActivityLogDAO implements IDAO<ActivityLog> {
    private static ActivityLogDAO instance;
    private static EntityManagerFactory emf;

    public static ActivityLogDAO getInstance(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new ActivityLogDAO();
        }
        return instance;
    }

    private ActivityLogDAO() {
        // Private constructor for singleton
    }

    @Override
    public ActivityLog create(ActivityLog activityLog) {
        // TODO: Implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<ActivityLog> getById(Long id) {
        // TODO: Implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Set<ActivityLog> getAll() {
        // TODO: Implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void update(ActivityLog activityLog) {
        // TODO: Implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void delete(Long id) {
        // TODO: Implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<ActivityLog> findByName(String name) {
        return Optional.empty();
    }

    // ========== CUSTOM BUSINESS METHODS ==========

    /**
     * Get activity logs for a customer
     * TODO: Implement
     */
    public Set<ActivityLog> getByCustomerId(Long customerId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Get logs by type (e.g., LOGIN, PAYMENT, SUBSCRIPTION_CHANGE)
     * TODO: Implement
     */
    public Set<ActivityLog> getByType(String type) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}

