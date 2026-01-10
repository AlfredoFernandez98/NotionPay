package dat.controllers.impl;

import dat.controllers.IController;
import dat.dtos.CustomerDTO;
import dat.dtos.SmsBalanceDTO;
import dat.entities.Customer;
import dat.entities.SmsBalance;
import dat.services.CustomerService;
import dat.utils.ErrorResponse;
import io.javalin.http.Context;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Controller for Customer endpoints
 * 
 * ARCHITECTURE: This controller ONLY uses Services (no DAOs)
 * All business logic is delegated to the Service layer
 */
public class CustomerController implements IController<CustomerDTO>{
    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    // ✅ ONLY Services (no DAOs)
    private final CustomerService customerService;

    public CustomerController(EntityManagerFactory emf) {
        this.customerService = CustomerService.getInstance(emf);
    }

    @Override
    public void read(Context ctx) {
        try {
            Long customerId = Long.parseLong(ctx.pathParam("id"));
            
            Optional<Customer> customerOpt = customerService.getById(customerId);
            
            if (customerOpt.isEmpty()) {
                ErrorResponse.notFound(ctx, "Customer not found");
                return;
            }
            
            Customer customer = customerOpt.get();
            
            // Convert Entity → DTO
            CustomerDTO dto = new CustomerDTO();
            dto.id = customer.getId();
            dto.email = customer.getUser().getEmail();
            dto.companyName = customer.getCompanyName();
            dto.serialNumber = customer.getSerialNumber();
            dto.externalCustomerId = customer.getExternalCustomerId();
            dto.createdAt = customer.getCreatedAt();
            
            ctx.status(200);
            ctx.json(dto);
            
        } catch (NumberFormatException e) {
            ErrorResponse.badRequest(ctx, "Invalid customer ID format");
        } catch (Exception e) {
            ErrorResponse.internalError(ctx, "Error fetching customer", logger, e);
        }
    }

    @Override
    public void readAll(Context ctx) {
        ErrorResponse.notImplemented(ctx, "Customers are managed by admins only");
    }

    @Override
    public void create(Context ctx) {
        try {
            CustomerDTO dto = ctx.bodyAsClass(CustomerDTO.class);

            if (dto.companyName == null || dto.companyName.isEmpty()) {
                ErrorResponse.badRequest(ctx, "Customer name cannot be empty");
                return;
            }

            if (dto.email == null || dto.email.isEmpty()) {
                ErrorResponse.badRequest(ctx, "Customer email cannot be empty");
                return;
            }

            if (dto.serialNumber == null) {
                ErrorResponse.badRequest(ctx, "Serial number cannot be empty");
                return;
            }

            // Delegate to service
            Customer savedCustomer = customerService.createCustomer(
                dto.email, 
                dto.companyName, 
                dto.serialNumber
            );
            
            // Convert Entity → DTO for response
            CustomerDTO responseDto = new CustomerDTO();
            responseDto.id = savedCustomer.getId();
            responseDto.email = savedCustomer.getUser().getEmail();
            responseDto.companyName = savedCustomer.getCompanyName();
            responseDto.serialNumber = savedCustomer.getSerialNumber();
            responseDto.externalCustomerId = savedCustomer.getExternalCustomerId();
            responseDto.createdAt = savedCustomer.getCreatedAt();
            
            // Return DTO response
            ctx.status(201);
            ctx.json(responseDto);

        } catch (CustomerService.CustomerServiceException e) {
            logger.error("Customer creation failed: {}", e.getMessage());
            ErrorResponse.badRequest(ctx, e.getMessage());
        } catch (Exception e) {
            ErrorResponse.internalError(ctx, "Internal server error", logger, e);
        }
    }

    @Override
    public void update(Context ctx) {
        ErrorResponse.notImplemented(ctx, "Customers are managed by admins only");
    }

    @Override
    public void delete(Context ctx) {
        ErrorResponse.notImplemented(ctx, "Customers are managed by admins only");
    }

    /**
     * GET /api/customers/{id}/sms-balance
     * Fetch SMS balance for customer
     */
    public void getSmsBalance(Context ctx) {
        try {
            Long customerId = Long.parseLong(ctx.pathParam("id"));
            
            // Get SMS balance from service
            Optional<SmsBalance> balance = customerService.getSmsBalance(customerId);
            
            if (balance.isPresent()) {
                SmsBalance entity = balance.get();
                
                // Convert Entity → DTO
                SmsBalanceDTO dto = new SmsBalanceDTO();
                dto.id = entity.getId();
                dto.externalCustomerId = entity.getExternalCustomerId();
                dto.remainingSmsCredits = entity.getRemainingSms();
                
                ctx.status(200);
                ctx.json(dto);
            } else {
                ErrorResponse.notFound(ctx, "SMS balance not found for customer");
            }

        } catch (NumberFormatException e) {
            ErrorResponse.badRequest(ctx, "Invalid customer ID format");
        } catch (CustomerService.CustomerServiceException e) {
            ErrorResponse.notFound(ctx, e.getMessage());
        } catch (Exception e) {
            ErrorResponse.internalError(ctx, "Error fetching SMS balance", logger, e);
        }
    }
}
