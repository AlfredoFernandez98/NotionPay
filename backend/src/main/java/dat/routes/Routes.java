package dat.routes;


import dat.config.HibernateConfig;
import dat.controllers.impl.CustomerController;
import dat.security.enums.Role;
import dat.services.SerialLinkVerificationService;
import io.javalin.apibuilder.EndpointGroup;
import jakarta.persistence.EntityManagerFactory;

import static io.javalin.apibuilder.ApiBuilder.*;

public class Routes {

    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    private static final CustomerController customerController = new CustomerController(emf, SerialLinkVerificationService.getInstance(emf));

    public EndpointGroup getRoutes() {
        return () -> {
            path("/customers", () -> {
                post("/", customerController::create, Role.ANYONE);  // Create customer
                get("/", customerController::readAll, Role.USER);     // Get all customers
                get("/{id}", customerController::read, Role.USER);    // Get one customer
                get("/{id}/sms-balance", customerController::getSmsBalance, Role.USER);  // Get SMS balance
                put("/{id}", customerController::update, Role.USER);  // Update customer
                delete("/{id}", customerController::delete, Role.ADMIN); // Delete customer
            });
        };
    }


}
