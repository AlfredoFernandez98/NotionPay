package dat.daos.impl;

import dat.daos.IDAO;
import dat.entities.Payment;
import dat.enums.PaymentStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DAO for Payment entity
 * Implements IDAO interface and provides custom Payment operations
 * Follows Singleton pattern with getInstance()
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
        return Optional.empty();
    }

    // ==================== Custom Payment Methods ====================

    /**
     * Get all payments for a specific customer
     * @param customerId The customer ID
     * @return Set of payments for the customer
     */
    public Set<Payment> getByCustomerId(Long customerId) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                    "SELECT p FROM Payment p WHERE p.customer.id = :customerId ORDER BY p.createdAt DESC",
                    Payment.class
            )
            .setParameter("customerId", customerId)
            .getResultList()
            .stream()
            .collect(Collectors.toSet());
        }
    }

    /**
     * Get payments by status
     * @param status The payment status
     * @return Set of payments with the given status
     */
    public Set<Payment> getByStatus(PaymentStatus status) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                    "SELECT p FROM Payment p WHERE p.status = :status ORDER BY p.createdAt DESC",
                    Payment.class
            )
            .setParameter("status", status)
            .getResultList()
            .stream()
            .collect(Collectors.toSet());
        }
    }

    /**
     * Get payment by Stripe PaymentIntent ID
     * @param processorIntentId Stripe PaymentIntent ID
     * @return Optional containing the Payment, or empty if not found
     */
    public Optional<Payment> getByProcessorIntentId(String processorIntentId) {
        try (EntityManager em = emf.createEntityManager()) {
            Payment payment = em.createQuery(
                    "SELECT p FROM Payment p WHERE p.processorIntentId = :intentId",
                    Payment.class
            )
            .setParameter("intentId", processorIntentId)
            .getSingleResult();
            return Optional.of(payment);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /**
     * Get payments for a specific subscription
     * @param subscriptionId The subscription ID
     * @return Set of payments for the subscription
     */
    public Set<Payment> getBySubscriptionId(Long subscriptionId) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                    "SELECT p FROM Payment p WHERE p.subscription.id = :subscriptionId ORDER BY p.createdAt DESC",
                    Payment.class
            )
            .setParameter("subscriptionId", subscriptionId)
            .getResultList()
            .stream()
            .collect(Collectors.toSet());
        }
    }

    /**
     * Get payments for a specific product purchase
     * @param productId The product ID
     * @return Set of payments for the product
     */
    public Set<Payment> getByProductId(Long productId) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                    "SELECT p FROM Payment p WHERE p.product.id = :productId ORDER BY p.createdAt DESC",
                    Payment.class
            )
            .setParameter("productId", productId)
            .getResultList()
            .stream()
            .collect(Collectors.toSet());
        }
    }
}

