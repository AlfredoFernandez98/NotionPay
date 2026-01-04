package dat.controllers.impl;

import dat.controllers.IController;
import dat.daos.impl.ProductDAO;
import dat.dtos.ProductDTO;
import dat.entities.Product;
import dat.utils.ErrorResponse;
import io.javalin.http.Context;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ProductController implements IController<ProductDTO> {
    
    private final ProductDAO productDAO;
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    
    public ProductController(EntityManagerFactory emf) {
        this.productDAO = ProductDAO.getInstance(emf);
    }

    /**
     * GET /api/products/{id}
     * Get single product by ID
     */
    @Override
    public void read(Context ctx) {
        try {
            Long id = Long.parseLong(ctx.pathParam("id"));
            Optional<Product> productOpt = productDAO.getById(id);
            
            if (productOpt.isPresent()) {
                ProductDTO dto = convertToDto(productOpt.get());
                ctx.status(200).json(dto);
                logger.info("Retrieved product ID: {}", id);
            } else {
                ErrorResponse.notFound(ctx, "Product not found with ID: " + id);
            }
            
        } catch (NumberFormatException e) {
            ErrorResponse.badRequest(ctx, "Invalid product ID format");
        } catch (Exception e) {
            ErrorResponse.internalError(ctx, "Error fetching product", logger, e);
        }
    }

    /**
     * GET /api/products
     * Get all products (SMS bundles)
     */
    @Override
    public void readAll(Context ctx) {
        try {
            Set<Product> products = productDAO.getAll();
            
            Set<ProductDTO> productsDTO = products.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toSet());
            
            ctx.status(200).json(productsDTO);
            logger.info("Retrieved {} products", productsDTO.size());
            
        } catch (Exception e) {
            ErrorResponse.internalError(ctx, "Error fetching products", logger, e);
        }
    }

    @Override
    public void create(Context ctx) {
        ErrorResponse.notImplemented(ctx, "Products are managed by admins only");
    }

    @Override
    public void update(Context ctx) {
        ErrorResponse.notImplemented(ctx, "Products are managed by admins only");
    }

    @Override
    public void delete(Context ctx) {
        ErrorResponse.notImplemented(ctx, "Products are managed by admins only");
    }

    /**
     * Helper: Convert Product entity to ProductDTO
     */
    private ProductDTO convertToDto(Product entity) {
        ProductDTO dto = new ProductDTO();
        dto.id = entity.getId();
        dto.productType = entity.getProductType();
        dto.name = entity.getName();
        dto.priceCents = entity.getPriceCents();
        dto.currency = entity.getCurrency();
        dto.description = entity.getDescription();
        dto.smsCount = entity.getSmsCount();
        return dto;
    }
}
