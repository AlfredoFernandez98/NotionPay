package dat.daos.impl;

import dat.daos.IDAO;
import dat.entities.SmsProduct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class SmsProductDAO implements IDAO<SmsProduct> {
    private static SmsProductDAO instance;
    private static EntityManagerFactory emf;
    
    public static SmsProductDAO getInstance(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new SmsProductDAO();
        }
        return instance;
    }
    
    private SmsProductDAO() {
    }

    @Override
    public SmsProduct create(SmsProduct entity) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(entity);
            em.getTransaction().commit();
            return entity;
        }
    }

    @Override
    public Optional<SmsProduct> getById(Long id) {
        try (EntityManager em = emf.createEntityManager()) {
            SmsProduct entity = em.find(SmsProduct.class, id);
            return Optional.ofNullable(entity);
        }
    }

    @Override
    public Set<SmsProduct> getAll() {
        try (EntityManager em = emf.createEntityManager()) {

            return em.createQuery("SELECT s FROM SmsProduct s ", SmsProduct.class)
                    .getResultList()
                    .stream()
                    .collect(Collectors.toSet());

        }
    }

    @Override
    public void update(SmsProduct entity) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.merge(entity);
            em.getTransaction().commit();
        }

    }

    @Override
    public void delete(Long id) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            SmsProduct entity = em.find(SmsProduct.class, id);
            if (entity != null) {
                em.remove(entity);
            }
            em.getTransaction().commit();
        }
    }

    @Override
    public Optional<SmsProduct> findByName(String name) {
        // SmsProduct doesn't have a name field directly
        // You'd search by product.name instead
        return Optional.empty();
    }

    /**
     * Get SmsProduct by Product ID
     */
    public Optional<SmsProduct> getByProductId(Long productId) {
        try (EntityManager em = emf.createEntityManager()) {
           SmsProduct smsProduct = em.createQuery("SElECT s FROM SmsProduct s WHERE s.product.id =:productID", SmsProduct.class)
                   .setParameter("productID", productId)
                   .getSingleResult();
           return Optional.of(smsProduct);
        }catch(NoResultException e){
            return Optional.empty();
        }
    }
}
