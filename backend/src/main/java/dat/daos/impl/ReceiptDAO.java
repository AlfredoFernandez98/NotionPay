package dat.daos.impl;

import dat.daos.IDAO;
import dat.entities.Receipt;
import dat.enums.ReceiptStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DAO for Receipt entity
 * Implements IDAO interface and provides custom Receipt operations
 * Follows Singleton pattern with getInstance()
 */
public class ReceiptDAO implements IDAO<Receipt> {
    private static ReceiptDAO instance;
    private static EntityManagerFactory emf;

 
    public static ReceiptDAO getInstance(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new ReceiptDAO();
        }
        return instance;
    }

    private ReceiptDAO() {
        // Private constructor for singleton
    }

    @Override
    public Receipt create(Receipt receipt) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(receipt);
            em.getTransaction().commit();
            return receipt;
        }
    }

    @Override
    public Optional<Receipt> getById(Long id) {
        try (EntityManager em = emf.createEntityManager()) {
            Receipt receipt = em.find(Receipt.class, id);
            return Optional.ofNullable(receipt);
        }
    }

    @Override
    public Set<Receipt> getAll() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT r FROM Receipt r", Receipt.class)
                    .getResultList()
                    .stream()
                    .collect(Collectors.toSet());
        }
    }

    @Override
    public void update(Receipt receipt) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.merge(receipt);
            em.getTransaction().commit();
        }
    }

    @Override
    public void delete(Long id) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Receipt receipt = em.find(Receipt.class, id);
            if (receipt != null) {
                em.remove(receipt);
            }
            em.getTransaction().commit();
        }
    }

    @Override
    public Optional<Receipt> findByName(String name) {
        return Optional.empty();
    }

    // ==================== Custom Receipt Methods ====================

    /**
     * Get receipt by payment ID
     * @param paymentId The payment ID
     * @return Optional containing the Receipt, or empty if not found
     */
    public Optional<Receipt> getByPaymentId(Long paymentId) {
        try (EntityManager em = emf.createEntityManager()) {
            Receipt receipt = em.createQuery(
                    "SELECT r FROM Receipt r WHERE r.payment.id = :paymentId",
                    Receipt.class
            )
            .setParameter("paymentId", paymentId)
            .getSingleResult();
            return Optional.of(receipt);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /**
     * Get receipt by receipt number
     * @param receiptNumber The unique receipt number
     * @return Optional containing the Receipt, or empty if not found
     */
    public Optional<Receipt> getByReceiptNumber(String receiptNumber) {
        try (EntityManager em = emf.createEntityManager()) {
            Receipt receipt = em.createQuery(
                    "SELECT r FROM Receipt r WHERE r.receiptNumber = :receiptNumber",
                    Receipt.class
            )
            .setParameter("receiptNumber", receiptNumber)
            .getSingleResult();
            return Optional.of(receipt);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /**
     * Get all receipts for a customer by email
     * @param customerEmail Customer email address
     * @return Set of receipts for the customer
     */
    public Set<Receipt> getByCustomerEmail(String customerEmail) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                    "SELECT r FROM Receipt r WHERE r.customerEmail = :email ORDER BY r.createdAt DESC",
                    Receipt.class
            )
            .setParameter("email", customerEmail)
            .getResultList()
            .stream()
            .collect(Collectors.toSet());
        }
    }

    /**
     * Get receipts by status
     * @param status The receipt status
     * @return Set of receipts with the given status
     */
    public Set<Receipt> getByStatus(ReceiptStatus status) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                    "SELECT r FROM Receipt r WHERE r.status = :status ORDER BY r.createdAt DESC",
                    Receipt.class
            )
            .setParameter("status", status)
            .getResultList()
            .stream()
            .collect(Collectors.toSet());
        }
    }

    /**
     * Get receipt by Stripe PaymentIntent ID
     * @param processorIntentId Stripe PaymentIntent ID
     * @return Optional containing the Receipt, or empty if not found
     */
    public Optional<Receipt> getByProcessorIntentId(String processorIntentId) {
        try (EntityManager em = emf.createEntityManager()) {
            Receipt receipt = em.createQuery(
                    "SELECT r FROM Receipt r WHERE r.processorIntentId = :intentId",
                    Receipt.class
            )
            .setParameter("intentId", processorIntentId)
            .getSingleResult();
            return Optional.of(receipt);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /**
     * Get all receipts for a customer by customer ID
     * @param customerId Customer ID
     * @return Set of receipts for the customer
     */
    public Set<Receipt> getByCustomerId(Long customerId) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                    "SELECT r FROM Receipt r WHERE r.payment.customer.id = :customerId ORDER BY r.createdAt DESC",
                    Receipt.class
            )
            .setParameter("customerId", customerId)
            .getResultList()
            .stream()
            .collect(Collectors.toSet());
        }
    }
}
