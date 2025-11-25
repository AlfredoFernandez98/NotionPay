package dat.controllers.impl;

import dat.controllers.IController;
import dat.daos.impl.CustomerDAO;
import dat.daos.impl.PaymentMethodDAO;
import dat.dtos.PaymentMethodDTO;
import dat.entities.Customer;
import dat.entities.PaymentMethod;
import dat.enums.PaymentMethodStatus;
import io.javalin.http.Context;
import jakarta.persistence.EntityManagerFactory;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * PaymentMethodController - Manages payment methods (credit cards, etc.)
 * 
 * Responsibilities:
 * 1. Add/save payment method for a customer
 * 2. Get all payment methods for a customer
 * 3. Set default payment method
 * 4. Delete/deactivate payment method
 */
public class PaymentMethodController implements IController<PaymentMethodDTO> {
    private final PaymentMethodDAO paymentMethodDAO;
    private final CustomerDAO customerDAO;

    public PaymentMethodController(EntityManagerFactory emf) {
        this.paymentMethodDAO = PaymentMethodDAO.getInstance(emf);
        this.customerDAO = CustomerDAO.getInstance(emf);
    }

    /**
     * GET /api/payment-methods/{id}
     * Get a payment method by ID
     */
    @Override
    public void read(Context ctx) {
        try {
            Long paymentMethodId = Long.parseLong(ctx.pathParam("id"));
            Optional<PaymentMethod> paymentMethod = paymentMethodDAO.getById(paymentMethodId);

            if (paymentMethod.isEmpty()) {
                ctx.status(404);
                ctx.json("Payment method not found");
                return;
            }

            ctx.status(200);
            ctx.json(convertToDTO(paymentMethod.get()));
        } catch (NumberFormatException e) {
            ctx.status(400);
            ctx.json("Invalid payment method ID format");
        } catch (Exception e) {
            ctx.status(500);
            ctx.json("Error: " + e.getMessage());
        }
    }

    /**
     * GET /api/payment-methods
     * Get all payment methods (admin only - not recommended for production)
     */
    @Override
    public void readAll(Context ctx) {
        try {
            var paymentMethods = paymentMethodDAO.getAll();
            ctx.status(200);
            ctx.json(paymentMethods.stream().map(this::convertToDTO).toList());
        } catch (Exception e) {
            ctx.status(500);
            ctx.json("Error: " + e.getMessage());
        }
    }

    /**
     * POST /api/payment-methods
     * Add a new payment method for a customer
     * 
     * Request body:
     * {
     *   "customerId": 1,
     *   "type": "card",
     *   "brand": "visa",
     *   "last4": "4242",
     *   "expMonth": 12,
     *   "expYear": 2025,
     *   "processorMethodId": "pm_xxx",
     *   "isDefault": true
     * }
     */
    @Override
    public void create(Context ctx) {
        try {
            PaymentMethodDTO paymentMethodDTO = ctx.bodyAsClass(PaymentMethodDTO.class);

            // ===== VALIDATION =====
            if (paymentMethodDTO.customerId == null) {
                ctx.status(400);
                ctx.json("Customer ID is required");
                return;
            }
            if (paymentMethodDTO.type == null || paymentMethodDTO.type.isEmpty()) {
                ctx.status(400);
                ctx.json("Payment method type is required");
                return;
            }
            if (paymentMethodDTO.processorMethodId == null || paymentMethodDTO.processorMethodId.isEmpty()) {
                ctx.status(400);
                ctx.json("Processor method ID is required");
                return;
            }

            // ===== FETCH CUSTOMER =====
            Optional<Customer> customerOpt = customerDAO.getById(paymentMethodDTO.customerId);
            if (customerOpt.isEmpty()) {
                ctx.status(404);
                ctx.json("Customer not found");
                return;
            }
            Customer customer = customerOpt.get();

            // ===== CREATE PAYMENT METHOD =====
            PaymentMethod paymentMethod = new PaymentMethod(
                    customer,
                    paymentMethodDTO.type,
                    paymentMethodDTO.brand,
                    paymentMethodDTO.last4,
                    paymentMethodDTO.expMonth,
                    paymentMethodDTO.expYear,
                    paymentMethodDTO.processorMethodId,
                    paymentMethodDTO.isDefault != null ? paymentMethodDTO.isDefault : false,
                    PaymentMethodStatus.ACTIVE,
                    paymentMethodDTO.fingerprint
            );

            PaymentMethod createdMethod = paymentMethodDAO.create(paymentMethod);

            ctx.status(201);
            ctx.json(convertToDTO(createdMethod));

        } catch (Exception e) {
            ctx.status(500);
            ctx.json("Error creating payment method: " + e.getMessage());
        }
    }

    /**
     * PUT /api/payment-methods/{id}
     * Update payment method (e.g., set as default, deactivate)
     */
    @Override
    public void update(Context ctx) {
        try {
            Long paymentMethodId = Long.parseLong(ctx.pathParam("id"));
            PaymentMethodDTO paymentMethodDTO = ctx.bodyAsClass(PaymentMethodDTO.class);

            Optional<PaymentMethod> paymentMethodOpt = paymentMethodDAO.getById(paymentMethodId);
            if (paymentMethodOpt.isEmpty()) {
                ctx.status(404);
                ctx.json("Payment method not found");
                return;
            }

            PaymentMethod paymentMethod = paymentMethodOpt.get();

            // Update fields if provided
            if (paymentMethodDTO.isDefault != null) {
                paymentMethod.setIsDefault(paymentMethodDTO.isDefault);
            }
            if (paymentMethodDTO.status != null) {
                paymentMethod.setStatus(paymentMethodDTO.status);
            }
            if (paymentMethodDTO.expMonth != null) {
                paymentMethod.setExpMonth(paymentMethodDTO.expMonth);
            }
            if (paymentMethodDTO.expYear != null) {
                paymentMethod.setExpYear(paymentMethodDTO.expYear);
            }

            paymentMethod.setUpdatedAt(OffsetDateTime.now());
            paymentMethodDAO.update(paymentMethod);

            ctx.status(200);
            ctx.json(convertToDTO(paymentMethod));
        } catch (NumberFormatException e) {
            ctx.status(400);
            ctx.json("Invalid payment method ID format");
        } catch (Exception e) {
            ctx.status(500);
            ctx.json("Error: " + e.getMessage());
        }
    }

    /**
     * DELETE /api/payment-methods/{id}
     * Deactivate/delete payment method
     */
    @Override
    public void delete(Context ctx) {
        try {
            Long paymentMethodId = Long.parseLong(ctx.pathParam("id"));

            Optional<PaymentMethod> paymentMethodOpt = paymentMethodDAO.getById(paymentMethodId);
            if (paymentMethodOpt.isEmpty()) {
                ctx.status(404);
                ctx.json("Payment method not found");
                return;
            }

            PaymentMethod paymentMethod = paymentMethodOpt.get();
            paymentMethod.setStatus(PaymentMethodStatus.INACTIVE);
            paymentMethod.setUpdatedAt(OffsetDateTime.now());
            paymentMethodDAO.update(paymentMethod);

            ctx.status(200);
            ctx.json("Payment method deleted successfully");

        } catch (NumberFormatException e) {
            ctx.status(400);
            ctx.json("Invalid payment method ID format");
        } catch (Exception e) {
            ctx.status(500);
            ctx.json("Error: " + e.getMessage());
        }
    }

    /**
     * GET /api/customers/{customerId}/payment-methods
     * Get all payment methods for a customer
     */
    public void getByCustomerId(Context ctx) {
        try {
            Long customerId = Long.parseLong(ctx.pathParam("customerId"));

            Optional<Customer> customerOpt = customerDAO.getById(customerId);
            if (customerOpt.isEmpty()) {
                ctx.status(404);
                ctx.json("Customer not found");
                return;
            }

            Customer customer = customerOpt.get();
            var paymentMethods = paymentMethodDAO.getByCustomer(customer);

            ctx.status(200);
            ctx.json(paymentMethods.stream().map(this::convertToDTO).collect(Collectors.toList()));

        } catch (NumberFormatException e) {
            ctx.status(400);
            ctx.json("Invalid customer ID format");
        } catch (Exception e) {
            ctx.status(500);
            ctx.json("Error: " + e.getMessage());
        }
    }

    /**
     * Helper method to convert PaymentMethod entity to DTO
     */
    private PaymentMethodDTO convertToDTO(PaymentMethod paymentMethod) {
        PaymentMethodDTO dto = new PaymentMethodDTO();
        dto.id = paymentMethod.getId();
        dto.customerId = paymentMethod.getCustomer().getId();
        dto.type = paymentMethod.getType();
        dto.brand = paymentMethod.getBrand();
        dto.last4 = paymentMethod.getLast4();
        dto.expMonth = paymentMethod.getExpMonth();
        dto.expYear = paymentMethod.getExpYear();
        dto.processorMethodId = paymentMethod.getProcessorMethodId();
        dto.isDefault = paymentMethod.getIsDefault();
        dto.status = paymentMethod.getStatus();
        dto.fingerprint = paymentMethod.getFingerprint();
        dto.createdAt = paymentMethod.getCreatedAt();
        dto.updatedAt = paymentMethod.getUpdatedAt();
        return dto;
    }
}
