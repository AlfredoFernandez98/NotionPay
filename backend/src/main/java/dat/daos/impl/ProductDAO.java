package dat.daos.impl;

import dat.daos.IDAO;
import dat.entities.Product;
import dat.enums.ProductType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ProductDAO implements IDAO<Product> {
    private static ProductDAO instance;
    private static EntityManagerFactory emf;

    public static ProductDAO getInstance(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new ProductDAO();
        }
        return instance;
    }

    private ProductDAO() {
    }

    @Override
    public Product create(Product product) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(product);
            em.getTransaction().commit();
            return product;
        }
    }

    @Override
    public Optional<Product> getById(Long id) {
        try (EntityManager em = emf.createEntityManager()) {
            Product product = em.find(Product.class, id);
            return Optional.ofNullable(product);
        }
    }

    @Override
    public Set<Product> getAll() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT p FROM Product p", Product.class)
                    .getResultList()
                    .stream()
                    .collect(Collectors.toSet());
        }
    }

    @Override
    public void update(Product product) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.merge(product);
            em.getTransaction().commit();
        }
    }

    @Override
    public void delete(Long id) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Product product = em.find(Product.class, id);
            if (product != null) {
                em.remove(product);
            }
            em.getTransaction().commit();
        }
    }

    @Override
    public Optional<Product> findByName(String name) {
        try (EntityManager em = emf.createEntityManager()) {
            Product product = em.createQuery("SELECT p FROM Product p WHERE p.name = :name", Product.class)
                    .setParameter("name", name)
                    .getSingleResult();
            return Optional.of(product);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /**
     * Get all products by type
     */
    public Set<Product> getByType(ProductType type) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                    "SELECT p FROM Product p WHERE p.productType = :type",
                    Product.class
            )
                    .setParameter("type", type)
                    .getResultList()
                    .stream()
                    .collect(Collectors.toSet());
        }
    }
}

