package dat.routes;


import dat.config.HibernateConfig;
import dat.controllers.impl.*;
import dat.security.enums.Role;
import dat.services.SerialLinkVerificationService;
import io.javalin.apibuilder.EndpointGroup;
import jakarta.persistence.EntityManagerFactory;

import static io.javalin.apibuilder.ApiBuilder.*;

public class Routes {

    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    private static final CustomerController customerController = new CustomerController(emf, SerialLinkVerificationService.getInstance(emf));
    private static final PlanController planController = new PlanController(emf);
    private static final SubscriptionController subscriptionController = new SubscriptionController(emf);
    private static final PaymentMethodController paymentMethodController = new PaymentMethodController(emf);
    private static final ProductController productController = new ProductController(emf);

    public EndpointGroup getRoutes() {
        return () -> {
            // ========== CUSTOMER ROUTES ==========
            path("/customers", () -> {
                post("/", customerController::create, Role.ANYONE);  // Create customer
                get("/", customerController::readAll, Role.USER);     // Get all customers
                get("/{id}", customerController::read, Role.USER);    // Get one customer
                get("/{id}/sms-balance", customerController::getSmsBalance, Role.USER);  // Get SMS balance
                put("/{id}", customerController::update, Role.USER);  // Update customer
                delete("/{id}", customerController::delete, Role.ADMIN); // Delete customer
            });

            // ========== PLAN ROUTES ==========
            path("/plans", () -> {
                get("/", planController::readAll, Role.ANYONE);       // Get all plans
                get("/active", planController::readAllActive, Role.ANYONE);  // Get active plans
                get("/{id}", planController::read, Role.ANYONE);      // Get one plan
                post("/", planController::create, Role.ADMIN);        // Create plan (admin only)
                put("/{id}", planController::update, Role.ADMIN);     // Update plan (admin only)
                delete("/{id}", planController::delete, Role.ADMIN);  // Delete plan (admin only)
            });

            // ========== SUBSCRIPTION ROUTES ==========
            path("/subscriptions", () -> {
                get("/", subscriptionController::readAll, Role.USER);           // Get all subscriptions
                get("/{id}", subscriptionController::read, Role.USER);          // Get one subscription
                post("/", subscriptionController::create, Role.USER);           // Create subscription (customer selects plan)
                put("/{id}", subscriptionController::update, Role.USER);        // Update subscription
                delete("/{id}", subscriptionController::delete, Role.USER);     // Cancel subscription
            });

            // ========== PAYMENT METHOD ROUTES ==========
            path("/payment-methods", () -> {
                get("/", paymentMethodController::readAll, Role.ADMIN);         // Get all (admin only)
                get("/{id}", paymentMethodController::read, Role.USER);         // Get one payment method
                post("/", paymentMethodController::create, Role.USER);          // Add payment method
                put("/{id}", paymentMethodController::update, Role.USER);       // Update payment method
                delete("/{id}", paymentMethodController::delete, Role.USER);    // Delete payment method
            });

            // Get payment methods for a specific customer
            path("/customers/{customerId}/payment-methods", () -> {
                get("/", paymentMethodController::getByCustomerId, Role.USER);  // Get customer's payment methods
            });

            // ========== PRODUCT ROUTES ==========
            path("/products", () -> {
                get("/", productController::readAll, Role.ANYONE);              // Get all products
                get("/{id}", productController::read, Role.ANYONE);             // Get one product
                get("/type/{type}", productController::getByType, Role.ANYONE); // Get products by type (SMS, PLAN, etc.)
                post("/", productController::create, Role.ADMIN);               // Create product (admin only)
                put("/{id}", productController::update, Role.ADMIN);            // Update product (admin only)
                delete("/{id}", productController::delete, Role.ADMIN);         // Delete product (admin only)
                post("/{productId}/purchase", productController::purchaseSmsProduct, Role.USER);  // Purchase SMS package
            });
        };
    }


}
