package dat.daos.impl;

import dat.daos.IDAO;
import dat.entities.ActivityLog;
import dat.entities.Customer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
        try(EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(activityLog);
            em.getTransaction().commit();
            return activityLog;
        }

    }

    @Override
    public Optional<ActivityLog> getById(Long id) {
       try(EntityManager em = emf.createEntityManager()) {
           ActivityLog activityLog = em.find(ActivityLog.class, id);
           return Optional.ofNullable(activityLog);

       }
    }

    @Override
    public Set<ActivityLog> getAll() {
      try(EntityManager em = emf.createEntityManager()) {
          return em.createQuery("SELECT a FROM ActivityLog a",ActivityLog.class )
                  .getResultList()
                  .stream()
                  .collect(Collectors.toSet());
      }
    }

    @Override
    public void update(ActivityLog activityLog) {
        try(EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.merge(activityLog);
            em.getTransaction().commit();
        }
    }

    @Override
    public void delete(Long id) {
        try(EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            ActivityLog activityLog = em.find(ActivityLog.class, id);
            if (activityLog != null) {
                em.remove(activityLog);
            }
            em.getTransaction().commit();
        }
    }

    @Override
    public Optional<ActivityLog> findByName(String name) {
        return Optional.empty();
    }

    // ========== CUSTOM BUSINESS METHODS ==========

    /**
     * Get all activity logs for a specific customer
     * @param customerId The customer ID to filter by
     * @return Set of ActivityLog entries for this customer
     */
    public Set<ActivityLog> getByCustomerId(Long customerId) {
        try(EntityManager em = emf.createEntityManager()) {
            // Verify customer exists
            Customer customer = em.find(Customer.class, customerId);
            if (customer == null) {
                return Collections.emptySet();
            }
            
            // Query all activity logs for this customer
            return em.createQuery(
                    "SELECT a FROM ActivityLog a WHERE a.customer.id = :customerId ORDER BY a.timestamp DESC",
                    ActivityLog.class)
                    .setParameter("customerId", customerId)
                    .getResultList()
                    .stream()
                    .collect(Collectors.toSet());
        }
    }

    /**
     * Get activity logs by type (e.g., LOGIN, LOGOUT, PAYMENT)
     * @param type The activity log type to filter by
     * @return Set of ActivityLog entries matching this type
     */
    public Set<ActivityLog> getByType(String type) {
        try(EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                    "SELECT a FROM ActivityLog a WHERE a.type = :type ORDER BY a.timestamp DESC",
                    ActivityLog.class)
                    .setParameter("type", type)
                    .getResultList()
                    .stream()
                    .collect(Collectors.toSet());
        }
    }
}

