package dat.daos.impl;

import dat.daos.IDAO;
import dat.entities.Plan;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class PlanDAO implements IDAO<Plan> {
    private static PlanDAO instance;
    private static EntityManagerFactory emf;

    public static PlanDAO getInstance(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new PlanDAO();
        }
        return instance;
    }

    private PlanDAO() {
    }

    @Override
    public Plan create(Plan plan) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(plan);
            em.getTransaction().commit();
            return plan;
        }
    }

    @Override
    public Optional<Plan> getById(Long id) {
        try (EntityManager em = emf.createEntityManager()) {
            Plan plan = em.find(Plan.class, id);
            return Optional.ofNullable(plan);
        }
    }

    @Override
    public Set<Plan> getAll() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT p FROM Plan p", Plan.class)
                    .getResultList()
                    .stream()
                    .collect(Collectors.toSet());
        }
    }

    @Override
    public void update(Plan plan) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.merge(plan);
            em.getTransaction().commit();
        }
    }

    @Override
    public void delete(Long id) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Plan plan = em.find(Plan.class, id);
            if (plan != null) {
                em.remove(plan);
            }
            em.getTransaction().commit();
        }
    }

    @Override
    public Optional<Plan> findByName(String name) {
        try (EntityManager em = emf.createEntityManager()) {
            Plan plan = em.createQuery("SELECT p FROM Plan p WHERE p.name = :name", Plan.class)
                    .setParameter("name", name)
                    .getSingleResult();
            return Optional.of(plan);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /**
     * Get all active plans
     */
    public Set<Plan> getAllActivePlans() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT p FROM Plan p WHERE p.active = true", Plan.class)
                    .getResultList()
                    .stream()
                    .collect(Collectors.toSet());
        }
    }
}
