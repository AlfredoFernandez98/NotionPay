package dat.services;

import dat.daos.impl.ProductDAO;
import dat.entities.Product;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Set;

/**
 * Service for managing products (SMS bundles)
 * Handles product retrieval operations
 * 
 * @author NotionPay Team
 */
public class ProductService {
    private static ProductService instance;
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    
    private final ProductDAO productDAO;

    public static ProductService getInstance(EntityManagerFactory emf) {
        if (instance == null) {
            instance = new ProductService(emf);
        }
        return instance;
    }

    private ProductService(EntityManagerFactory emf) {
        this.productDAO = ProductDAO.getInstance(emf);
        logger.info("ProductService initialized");
    }

    /**
     * Get product by ID
     */
    public Optional<Product> getById(Long id) {
        return productDAO.getById(id);
    }

    /**
     * Get all products
     */
    public Set<Product> getAll() {
        return productDAO.getAll();
    }
}
