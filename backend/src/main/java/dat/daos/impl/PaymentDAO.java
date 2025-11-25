package dat.daos.impl;

import dat.daos.IDAO;
import dat.entities.Payment;
import dat.enums.PaymentStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DAO for Payment entity
 * Handles all database operations for payments
 */
public class PaymentDAO implements IDAO<Payment> {
    private static PaymentDAO instance;
    private static EntityManagerFactory emf;

    public static PaymentDAO getInstance(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new PaymentDAO();
        }
        return instance;
    }

    private PaymentDAO() {
        // Private constructor for singleton
    }

    @Override
    public Payment create(Payment payment) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(payment);
            em.getTransaction().commit();
            return payment;
        }
    }

    @Override
    public Optional<Payment> getById(Long id) {
        try (EntityManager em = emf.createEntityManager()) {
            Payment payment = em.find(Payment.class, id);
            return Optional.ofNullable(payment);
        }
    }

    @Override
    public Set<Payment> getAll() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT p FROM Payment p", Payment.class)
                    .getResultList()
                    .stream()
                    .collect(Collectors.toSet());
        }
    }

    @Override
    public void update(Payment payment) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.merge(payment);
            em.getTransaction().commit();
        }
    }

    @Override
    public void delete(Long id) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Payment payment = em.find(Payment.class, id);
            if (payment != null) {
                em.remove(payment);
            }
            em.getTransaction().commit();
        }
    }

    @Override
    public Optional<Payment> findByName(String name) {
        return Optional.empty();  // Payment doesn't have a name field
    }

    // ========== CUSTOM BUSINESS METHODS ==========

    /**
     * Get all payments for a specific customer
     * @param customerId The customer ID to filter by
     * @return Set of Payment entries for this customer
     */
    public Set<Payment> getByCustomerId(Long customerId) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                    "SELECT p FROM Payment p WHERE p.customer.id = :customerId ORDER BY p.createdAt DESC",
                    Payment.class)
                    .setParameter("customerId", customerId)
                    .getResultList()
                    .stream()
                    .collect(Collectors.toSet());
        }
    }

    /**
     * Get payments by status
     * @param status The payment status to filter by
     * @return Set of Payment entries matching this status
     */
    public Set<Payment> getByStatus(PaymentStatus status) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                    "SELECT p FROM Payment p WHERE p.status = :status ORDER BY p.createdAt DESC",
                    Payment.class)
                    .setParameter("status", status)
                    .getResultList()
                    .stream()
                    .collect(Collectors.toSet());
        }
    }

    /**
     * Get payments for a specific subscription
     * @param subscriptionId The subscription ID
     * @return Set of payments for this subscription
     */
    public Set<Payment> getBySubscriptionId(Long subscriptionId) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                    "SELECT p FROM Payment p WHERE p.subscription.id = :subscriptionId ORDER BY p.createdAt DESC",
                    Payment.class)
                    .setParameter("subscriptionId", subscriptionId)
                    .getResultList()
                    .stream()
                    .collect(Collectors.toSet());
        }
    }
}

