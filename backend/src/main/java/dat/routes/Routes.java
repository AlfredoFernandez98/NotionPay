package dat.routes;


import dat.config.HibernateConfig;
import dat.controllers.impl.CustomerController;
import dat.controllers.impl.PlanController;
import dat.controllers.impl.ProductController;
import dat.controllers.impl.SubscriptionController;
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
    private static final ProductController productController = new ProductController(emf);

    public EndpointGroup getRoutes() {
        return () -> {
            path("/customers", () -> {
                post("/", customerController::create, Role.ANYONE);  // Create customer
                get("/", customerController::readAll, Role.USER);     // Get all customers
                get("/{id}", customerController::read, Role.USER);    // Get one customer
                get("/{id}/sms-balance", customerController::getSmsBalance, Role.USER);  // Get SMS balance
                get("/{customerId}/subscription", subscriptionController::getCustomerSubscription, Role.USER);  // Get customer's subscription
                put("/{id}", customerController::update, Role.USER);  // Update customer
                delete("/{id}", customerController::delete, Role.ADMIN); // Delete customer
            });
            
            path("/plans", () -> {
                get("/", planController::readAll, Role.ANYONE);  // Get all active plans
                get("/{id}", planController::read, Role.ANYONE); // Get one plan
            });
            
            path("/subscriptions", () -> {
                get("/{id}", subscriptionController::read, Role.USER);  // Get subscription by ID
                put("/{id}/cancel", subscriptionController::cancel, Role.USER);  // Cancel subscription
            });
            
            path("/products", () -> {
                get("/", productController::readAll, Role.ANYONE);  // Get all SMS products
                get("/{id}", productController::read, Role.ANYONE); // Get one SMS product

            });
        };
    }


}
