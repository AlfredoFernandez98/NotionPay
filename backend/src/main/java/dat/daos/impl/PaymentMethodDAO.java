package dat.daos.impl;

import dat.daos.IDAO;
import dat.entities.Customer;
import dat.entities.PaymentMethod;
import dat.enums.PaymentMethodStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class PaymentMethodDAO implements IDAO<PaymentMethod> {
    private static PaymentMethodDAO instance;
    private static EntityManagerFactory emf;

    public static PaymentMethodDAO getInstance(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new PaymentMethodDAO();
        }
        return instance;
    }

    private PaymentMethodDAO() {
    }

    @Override
    public PaymentMethod create(PaymentMethod paymentMethod) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(paymentMethod);
            em.getTransaction().commit();
            return paymentMethod;
        }
    }

    @Override
    public Optional<PaymentMethod> getById(Long id) {
        try (EntityManager em = emf.createEntityManager()) {
            PaymentMethod paymentMethod = em.find(PaymentMethod.class, id);
            return Optional.ofNullable(paymentMethod);
        }
    }

    @Override
    public Set<PaymentMethod> getAll() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT pm FROM PaymentMethod pm", PaymentMethod.class)
                    .getResultList()
                    .stream()
                    .collect(Collectors.toSet());
        }
    }

    @Override
    public void update(PaymentMethod paymentMethod) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.merge(paymentMethod);
            em.getTransaction().commit();
        }
    }

    @Override
    public void delete(Long id) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            PaymentMethod paymentMethod = em.find(PaymentMethod.class, id);
            if (paymentMethod != null) {
                em.remove(paymentMethod);
            }
            em.getTransaction().commit();
        }
    }

    @Override
    public Optional<PaymentMethod> findByName(String name) {
        return Optional.empty();
    }

    /**
     * Get all payment methods for a customer
     */
    public Set<PaymentMethod> getByCustomer(Customer customer) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                    "SELECT pm FROM PaymentMethod pm WHERE pm.customer = :customer AND pm.status = :status",
                    PaymentMethod.class
            )
                    .setParameter("customer", customer)
                    .setParameter("status", PaymentMethodStatus.ACTIVE)
                    .getResultList()
                    .stream()
                    .collect(Collectors.toSet());
        }
    }

    /**
     * Get default payment method for a customer
     */
    public Optional<PaymentMethod> getDefaultByCustomer(Customer customer) {
        try (EntityManager em = emf.createEntityManager()) {
            PaymentMethod paymentMethod = em.createQuery(
                    "SELECT pm FROM PaymentMethod pm WHERE pm.customer = :customer AND pm.isDefault = true AND pm.status = :status",
                    PaymentMethod.class
            )
                    .setParameter("customer", customer)
                    .setParameter("status", PaymentMethodStatus.ACTIVE)
                    .getSingleResult();
            return Optional.of(paymentMethod);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}

