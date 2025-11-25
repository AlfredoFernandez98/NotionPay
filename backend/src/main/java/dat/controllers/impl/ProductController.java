package dat.controllers.impl;

import dat.controllers.IController;
import dat.daos.impl.*;
import dat.dtos.ProductDTO;
import dat.entities.*;
import dat.enums.PaymentStatus;
import dat.enums.ProductType;
import io.javalin.http.Context;
import jakarta.persistence.EntityManagerFactory;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ProductController - Manages products (SMS packages, add-ons, etc.)
 * 
 * Responsibilities:
 * 1. List all products
 * 2. List products by type (SMS, PLAN, etc.)
 * 3. Get product details
 * 4. Admin: Create, update, delete products
 */
public class ProductController implements IController<ProductDTO> {
    private final ProductDAO productDAO;
    private final SmsProductDAO smsProductDAO;
    private final CustomerDAO customerDAO;
    private final PaymentMethodDAO paymentMethodDAO;
    private final PaymentDAO paymentDAO;
    private final SmsBalanceDAO smsBalanceDAO;

    public ProductController(EntityManagerFactory emf) {
        this.productDAO = ProductDAO.getInstance(emf);
        this.smsProductDAO = SmsProductDAO.getInstance(emf);
        this.customerDAO = CustomerDAO.getInstance(emf);
        this.paymentMethodDAO = PaymentMethodDAO.getInstance(emf);
        this.paymentDAO = PaymentDAO.getInstance(emf);
        this.smsBalanceDAO = SmsBalanceDAO.getInstance(emf);
    }

    /**
     * GET /api/products/{id}
     * Get product by ID
     */
    @Override
    public void read(Context ctx) {
        try {
            Long productId = Long.parseLong(ctx.pathParam("id"));
            Optional<Product> product = productDAO.getById(productId);

            if (product.isEmpty()) {
                ctx.status(404);
                ctx.json("Product not found");
                return;
            }

            ctx.status(200);
            ctx.json(convertToDTO(product.get()));
        } catch (NumberFormatException e) {
            ctx.status(400);
            ctx.json("Invalid product ID format");
        } catch (Exception e) {
            ctx.status(500);
            ctx.json("Error: " + e.getMessage());
        }
    }

    /**
     * GET /api/products
     * Get all products
     */
    @Override
    public void readAll(Context ctx) {
        try {
            var products = productDAO.getAll();
            ctx.status(200);
            ctx.json(products.stream().map(this::convertToDTO).toList());
        } catch (Exception e) {
            ctx.status(500);
            ctx.json("Error: " + e.getMessage());
        }
    }

    /**
     * GET /api/products/type/{type}
     * Get products by type (SMS, PLAN, ADD_ON, etc.)
     */
    public void getByType(Context ctx) {
        try {
            String typeStr = ctx.pathParam("type");
            ProductType type = ProductType.valueOf(typeStr.toUpperCase());

            var products = productDAO.getByType(type);
            ctx.status(200);
            ctx.json(products.stream().map(this::convertToDTO).collect(Collectors.toList()));

        } catch (IllegalArgumentException e) {
            ctx.status(400);
            ctx.json("Invalid product type: " + e.getMessage());
        } catch (Exception e) {
            ctx.status(500);
            ctx.json("Error: " + e.getMessage());
        }
    }

    /**
     * POST /api/products
     * Create a new product (ADMIN ONLY)
     * 
     * Request body:
     * {
     *   "productType": "SMS",
     *   "name": "SMS Package 100",
     *   "priceCents": 9999,
     *   "currency": "DKK",
     *   "description": "100 SMS messages"
     * }
     */
    @Override
    public void create(Context ctx) {
        try {
            ProductDTO productDTO = ctx.bodyAsClass(ProductDTO.class);

            // ===== VALIDATION =====
            if (productDTO.productType == null) {
                ctx.status(400);
                ctx.json("Product type is required");
                return;
            }
            if (productDTO.name == null || productDTO.name.isEmpty()) {
                ctx.status(400);
                ctx.json("Product name is required");
                return;
            }
            if (productDTO.priceCents == null || productDTO.priceCents < 0) {
                ctx.status(400);
                ctx.json("Product price must be valid");
                return;
            }

            // ===== CREATE PRODUCT =====
            Product product = new Product(
                    productDTO.productType,
                    productDTO.name,
                    productDTO.priceCents,
                    productDTO.currency,
                    productDTO.description
            );

            Product createdProduct = productDAO.create(product);

            ctx.status(201);
            ctx.json(convertToDTO(createdProduct));

        } catch (Exception e) {
            ctx.status(500);
            ctx.json("Error creating product: " + e.getMessage());
        }
    }

    /**
     * PUT /api/products/{id}
     * Update product
     */
    @Override
    public void update(Context ctx) {
        try {
            Long productId = Long.parseLong(ctx.pathParam("id"));
            ProductDTO productDTO = ctx.bodyAsClass(ProductDTO.class);

            Optional<Product> productOpt = productDAO.getById(productId);
            if (productOpt.isEmpty()) {
                ctx.status(404);
                ctx.json("Product not found");
                return;
            }

            Product product = productOpt.get();

            // Update fields if provided
            if (productDTO.name != null) {
                product.setName(productDTO.name);
            }
            if (productDTO.productType != null) {
                product.setProductType(productDTO.productType);
            }
            if (productDTO.priceCents != null) {
                product.setPriceCents(productDTO.priceCents);
            }
            if (productDTO.currency != null) {
                product.setCurrency(productDTO.currency);
            }
            if (productDTO.description != null) {
                product.setDescription(productDTO.description);
            }

            productDAO.update(product);

            ctx.status(200);
            ctx.json(convertToDTO(product));
        } catch (NumberFormatException e) {
            ctx.status(400);
            ctx.json("Invalid product ID format");
        } catch (Exception e) {
            ctx.status(500);
            ctx.json("Error: " + e.getMessage());
        }
    }

    /**
     * DELETE /api/products/{id}
     * Delete product
     */
    @Override
    public void delete(Context ctx) {
        try {
            Long productId = Long.parseLong(ctx.pathParam("id"));

            Optional<Product> product = productDAO.getById(productId);
            if (product.isEmpty()) {
                ctx.status(404);
                ctx.json("Product not found");
                return;
            }

            productDAO.delete(productId);

            ctx.status(200);
            ctx.json("Product deleted successfully");

        } catch (NumberFormatException e) {
            ctx.status(400);
            ctx.json("Invalid product ID format");
        } catch (Exception e) {
            ctx.status(500);
            ctx.json("Error: " + e.getMessage());
        }
    }

    /**
     * POST /api/products/{productId}/purchase
     * Customer purchases an SMS package
     * 
     * Request body:
     * {
     *   "customerId": 1,
     *   "paymentMethodId": 2
     * }
     */
    public void purchaseSmsProduct(Context ctx) {
        try {
            // ===== 1. GET PRODUCT ID FROM URL =====
            Long productId = Long.parseLong(ctx.pathParam("productId"));

            // ===== 2. PARSE REQUEST BODY =====
            var requestBody = ctx.bodyAsClass(java.util.Map.class);
            Long customerId = Long.parseLong(requestBody.get("customerId").toString());
            Long paymentMethodId = Long.parseLong(requestBody.get("paymentMethodId").toString());

            // ===== 3. VALIDATE PRODUCT EXISTS =====
            Optional<Product> productOpt = productDAO.getById(productId);
            if (productOpt.isEmpty()) {
                ctx.status(404);
                ctx.json("Product not found");
                return;
            }
            Product product = productOpt.get();

            // ===== 4. VALIDATE IT'S AN SMS PRODUCT =====
            if (product.getProductType() != ProductType.SMS) {
                ctx.status(400);
                ctx.json("This product is not an SMS package");
                return;
            }

            // ===== 4b. GET SMS PRODUCT DETAILS =====
            Optional<SmsProduct> smsProductOpt = smsProductDAO.getByProductId(productId);
            if (smsProductOpt.isEmpty()) {
                ctx.status(404);
                ctx.json("SMS product details not found");
                return;
            }
            SmsProduct smsProduct = smsProductOpt.get();

            // ===== 5. VALIDATE CUSTOMER EXISTS =====
            Optional<Customer> customerOpt = customerDAO.getById(customerId);
            if (customerOpt.isEmpty()) {
                ctx.status(404);
                ctx.json("Customer not found");
                return;
            }
            Customer customer = customerOpt.get();

            // ===== 6. VALIDATE PAYMENT METHOD EXISTS =====
            Optional<PaymentMethod> paymentMethodOpt = paymentMethodDAO.getById(paymentMethodId);
            if (paymentMethodOpt.isEmpty()) {
                ctx.status(404);
                ctx.json("Payment method not found");
                return;
            }
            PaymentMethod paymentMethod = paymentMethodOpt.get();

            // ===== 7. VALIDATE PAYMENT METHOD BELONGS TO CUSTOMER =====
            if (!paymentMethod.getCustomer().getId().equals(customerId)) {
                ctx.status(403);
                ctx.json("Payment method does not belong to this customer");
                return;
            }

            // ===== 8. CREATE PAYMENT RECORD =====
            Payment payment = new Payment(
                    customer,
                    paymentMethod,
                    null,  // No subscription for one-time purchase
                    product,
                    PaymentStatus.COMPLETED,  // In real app, you'd process payment first
                    product.getPriceCents(),
                    product.getCurrency(),
                    "intent_" + System.currentTimeMillis()  // Mock processor intent ID
            );
            Payment createdPayment = paymentDAO.create(payment);

            // ===== 9. UPDATE SMS BALANCE =====
            // Get customer's SMS balance
            Optional<SmsBalance> balanceOpt = smsBalanceDAO.getByExternalCustomerId(customer.getExternalCustomerId());
            
            int smsCount = smsProduct.getSmsCount(); // Get actual SMS count from SmsProduct
            
            if (balanceOpt.isPresent()) {
                // Update existing balance
                SmsBalance balance = balanceOpt.get();
                balance.recharge(smsCount);  // Add SMS from product
                smsBalanceDAO.update(balance);
            } else {
                // Create new balance if doesn't exist
                SmsBalance newBalance = new SmsBalance(
                        customer.getExternalCustomerId(),
                        smsCount  // Start with SMS from product
                );
                smsBalanceDAO.create(newBalance);
            }

            // ===== 10. RETURN SUCCESS =====
            ctx.status(201);
            ctx.json(new java.util.HashMap<String, Object>() {{
                put("message", "SMS package purchased successfully");
                put("paymentId", createdPayment.getId());
                put("productName", product.getName());
                put("price", product.getPriceCents());
                put("smsAdded", smsCount);
            }});

        } catch (NumberFormatException e) {
            ctx.status(400);
            ctx.json("Invalid ID format");
        } catch (Exception e) {
            ctx.status(500);
            ctx.json("Error purchasing SMS package: " + e.getMessage());
        }
    }

    /**
     * Helper method to convert Product entity to DTO
     * If it's an SMS product, also includes smsCount from SmsProduct
     */
    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.id = product.getId();
        dto.productType = product.getProductType();
        dto.name = product.getName();
        dto.priceCents = product.getPriceCents();
        dto.currency = product.getCurrency();
        dto.description = product.getDescription();
        
        // If it's an SMS product, fetch and include SMS count
        if (product.getProductType() == ProductType.SMS) {
            Optional<SmsProduct> smsProductOpt = smsProductDAO.getByProductId(product.getId());
            smsProductOpt.ifPresent(smsProduct -> dto.smsCount = smsProduct.getSmsCount());
        }
        
        return dto;
    }
}
