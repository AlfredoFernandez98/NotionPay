package dat.daos.impl;

import dat.daos.IDAO;
import dat.entities.SerialLink;
import dat.enums.Status;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class SerialLinkDAO implements IDAO<SerialLink> {
    private static SerialLinkDAO instance;
    private static EntityManagerFactory emf;

    public static SerialLinkDAO getInstance(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new SerialLinkDAO();
        }
        return instance;
    }

    private SerialLinkDAO() {
    }

    @Override
    public SerialLink create(SerialLink serialLink) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(serialLink);
            em.getTransaction().commit();
            return serialLink;
        }
    }

    @Override
    public Optional<SerialLink> getById(Long id) {
        try (EntityManager em = emf.createEntityManager()) {
            SerialLink serialLink = em.find(SerialLink.class, id);
            return Optional.ofNullable(serialLink);
        }
    }

    @Override
    public Set<SerialLink> getAll() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT s FROM SerialLink s", SerialLink.class)
                    .getResultList()
                    .stream()
                    .collect(Collectors.toSet());
        }
    }

    @Override
    public void update(SerialLink serialLink) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.merge(serialLink);
            em.getTransaction().commit();
        }
    }

    @Override
    public void delete(Long id) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            SerialLink serialLink = em.find(SerialLink.class, id);
            if (serialLink != null) {
                em.remove(serialLink);
            }
            em.getTransaction().commit();
        }
    }

    @Override
    public Optional<SerialLink> findByName(String name) {
        // SerialLink doesn't have a name field, return empty
        return Optional.empty();
    }

    /**
     * Find SerialLink by serial number
     */
    public Optional<SerialLink> findBySerialNumber(Integer serialNumber) {
        try (EntityManager em = emf.createEntityManager()) {
            SerialLink serialLink = em.createQuery(
                    "SELECT s FROM SerialLink s WHERE s.serialNumber = :serialNumber",
                    SerialLink.class
            )
                    .setParameter("serialNumber", serialNumber)
                    .getSingleResult();
            return Optional.of(serialLink);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /**
     * Get all SerialLinks by status
     */
    public Set<SerialLink> getByStatus(Status status) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                    "SELECT s FROM SerialLink s WHERE s.status = :status",
                    SerialLink.class
            )
                    .setParameter("status", status)
                    .getResultList()
                    .stream()
                    .collect(Collectors.toSet());
        }
    }
}
