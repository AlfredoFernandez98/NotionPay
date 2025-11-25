package dat.controllers.impl;

import dat.controllers.IController;
import dat.daos.impl.PaymentDAO;
import dat.dtos.PaymentDTO;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import jakarta.persistence.EntityManagerFactory;

/**
 * Controller for Payment endpoints
 * TODO: Implement IController interface
 * TODO: Add handlers for:
 *  - createPayment (POST /api/payments)
 *  - getPayment (GET /api/payments/{id})
 *  - getAllPayments (GET /api/payments)
 *  - getCustomerPayments (GET /api/customers/{id}/payments)
 */
public class PaymentController implements IController<PaymentDTO> {
    private final PaymentDAO paymentDAO;

    public PaymentController(EntityManagerFactory emf) {
        this.paymentDAO = PaymentDAO.getInstance(emf);
    }

    /**
     * Create a new payment
     * TODO: Parse PaymentDTO from request body
     * TODO: Validate data
     * TODO: Create Payment entity
     * TODO: Save via DAO
     * TODO: Return 201 Created with payment details
     */
    public Handler createPayment() {
        return ctx -> {
            // TODO: Implement
            ctx.status(501).result("Not implemented yet");
        };
    }

    /**
     * Get payment by ID
     * TODO: Get ID from path parameter
     * TODO: Query DAO
     * TODO: Return 200 OK or 404 Not Found
     */
    public Handler getPayment() {
        return ctx -> {
            // TODO: Implement
            ctx.status(501).result("Not implemented yet");
        };
    }

    /**
     * Get all payments
     * TODO: Query DAO
     * TODO: Convert to DTOs
     * TODO: Return 200 OK with list
     */
    public Handler getAllPayments() {
        return ctx -> {
            // TODO: Implement
            ctx.status(501).result("Not implemented yet");
        };
    }

    /**
     * Get payments for a specific customer
     * TODO: Get customerId from path parameter
     * TODO: Query DAO with custom method
     * TODO: Return 200 OK with list
     */
    public Handler getCustomerPayments() {
        return ctx -> {
            // TODO: Implement
            ctx.status(501).result("Not implemented yet");
        };
    }

    @Override
    public void read(Context ctx) {

    }

    @Override
    public void readAll(Context ctx) {

    }

    @Override
    public void create(Context ctx) {

    }

    @Override
    public void update(Context ctx) {

    }

    @Override
    public void delete(Context ctx) {

    }
}

