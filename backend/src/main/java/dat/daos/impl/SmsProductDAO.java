package dat.daos.impl;

import dat.daos.IDAO;
import dat.entities.SmsProduct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DAO for SmsProduct entity
 * Manages SMS-specific product details (smsCount) linked to Product
 */
public class SmsProductDAO implements IDAO<SmsProduct> {
    private static SmsProductDAO instance;
    private static EntityManagerFactory emf;

    private SmsProductDAO(EntityManagerFactory emf) {
        SmsProductDAO.emf = emf;
    }

    public static SmsProductDAO getInstance(EntityManagerFactory emf) {
        if (instance == null) {
            instance = new SmsProductDAO(emf);
        }
        return instance;
    }

    @Override
    public SmsProduct create(SmsProduct smsProduct) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(smsProduct);
            em.getTransaction().commit();
            return smsProduct;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Failed to create SmsProduct", e);
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<SmsProduct> getById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            SmsProduct smsProduct = em.find(SmsProduct.class, id);
            return Optional.ofNullable(smsProduct);
        } finally {
            em.close();
        }
    }

    @Override
    public Set<SmsProduct> getAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT sp FROM SmsProduct sp", SmsProduct.class)
                    .getResultStream()
                    .collect(Collectors.toSet());
        } finally {
            em.close();
        }
    }

    @Override
    public void update(SmsProduct smsProduct) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(smsProduct);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Failed to update SmsProduct", e);
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            SmsProduct smsProduct = em.find(SmsProduct.class, id);
            if (smsProduct != null) {
                em.remove(smsProduct);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Failed to delete SmsProduct", e);
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<SmsProduct> findByName(String name) {
        EntityManager em = emf.createEntityManager();
        try {
            SmsProduct smsProduct = em.createQuery(
                    "SELECT sp FROM SmsProduct sp WHERE sp.product.name = :name", 
                    SmsProduct.class)
                    .setParameter("name", name)
                    .getSingleResult();
            return Optional.of(smsProduct);
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    /**
     * Get SmsProduct by Product ID
     * Useful for finding SMS details when you have a Product
     */
    public Optional<SmsProduct> getByProductId(Long productId) {
        EntityManager em = emf.createEntityManager();
        try {
            SmsProduct smsProduct = em.createQuery(
                    "SELECT sp FROM SmsProduct sp WHERE sp.product.id = :productId", 
                    SmsProduct.class)
                    .setParameter("productId", productId)
                    .getSingleResult();
            return Optional.of(smsProduct);
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }
}
