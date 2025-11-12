package dat.daos.impl;

import dat.daos.IDAO;
import dat.entities.Customer;
import dat.entities.Subscription;
import dat.enums.SubscriptionStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class SubscriptionDAO implements IDAO<Subscription> {
    private static SubscriptionDAO instance;
    private static EntityManagerFactory emf;

    public static SubscriptionDAO getInstance(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new SubscriptionDAO();
        }
        return instance;
    }

    private SubscriptionDAO() {
    }

    @Override
    public Subscription create(Subscription subscription) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(subscription);
            em.getTransaction().commit();
            return subscription;
        }
    }

    @Override
    public Optional<Subscription> getById(Long id) {
        try (EntityManager em = emf.createEntityManager()) {
            Subscription subscription = em.find(Subscription.class, id);
            return Optional.ofNullable(subscription);
        }
    }

    @Override
    public Set<Subscription> getAll() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT s FROM Subscription s", Subscription.class)
                    .getResultList()
                    .stream()
                    .collect(Collectors.toSet());
        }
    }

    @Override
    public void update(Subscription subscription) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.merge(subscription);
            em.getTransaction().commit();
        }
    }

    @Override
    public void delete(Long id) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Subscription subscription = em.find(Subscription.class, id);
            if (subscription != null) {
                em.remove(subscription);
            }
            em.getTransaction().commit();
        }
    }

    @Override
    public Optional<Subscription> findByName(String name) {
        return Optional.empty();
    }

    /**
     * Get all subscriptions for a customer
     */
    public Set<Subscription> getByCustomer(Customer customer) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                    "SELECT s FROM Subscription s WHERE s.customer = :customer",
                    Subscription.class
            )
                    .setParameter("customer", customer)
                    .getResultList()
                    .stream()
                    .collect(Collectors.toSet());
        }
    }

    /**
     * Get active subscription for a customer
     */
    public Optional<Subscription> getActiveSubscriptionByCustomer(Customer customer) {
        try (EntityManager em = emf.createEntityManager()) {
            Subscription subscription = em.createQuery(
                    "SELECT s FROM Subscription s WHERE s.customer = :customer AND s.status = :status",
                    Subscription.class
            )
                    .setParameter("customer", customer)
                    .setParameter("status", SubscriptionStatus.ACTIVE)
                    .getSingleResult();
            return Optional.of(subscription);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}

