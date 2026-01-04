package dat.controllers.impl;

import dat.controllers.IController;
import dat.daos.impl.CustomerDAO;
import dat.daos.impl.SmsBalanceDAO;
import dat.daos.impl.SubscriptionDAO;
import dat.dtos.CustomerDTO;
import dat.dtos.SmsBalanceDTO;
import dat.entities.Customer;
import dat.entities.Plan;
import dat.entities.SmsBalance;
import dat.entities.Subscription;
import dat.enums.AnchorPolicy;
import dat.enums.SubscriptionStatus;
import dat.security.daos.ISecurityDAO;
import dat.security.daos.SecurityDAO;
import dat.security.entities.User;
import dat.services.SerialLinkVerificationService;
import dat.utils.DateTimeUtil;
import dat.utils.ErrorResponse;
import io.javalin.http.Context;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.Optional;

public class CustomerController implements IController<CustomerDTO>{
    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    private final CustomerDAO customerDAO;
    private final SmsBalanceDAO smsBalanceDAO;
    private final SubscriptionDAO subscriptionDAO;
    private final SerialLinkVerificationService serialLinkService;
    private final ISecurityDAO securityDAO;


    public CustomerController(EntityManagerFactory emf, SerialLinkVerificationService serialLinkService) {
        this.customerDAO = CustomerDAO.getInstance(emf);
        this.smsBalanceDAO = SmsBalanceDAO.getInstance(emf);
        this.subscriptionDAO = SubscriptionDAO.getInstance(emf);
        this.serialLinkService = SerialLinkVerificationService.getInstance(emf);
        this.securityDAO = new SecurityDAO(emf);
    }
    @Override
    public void read(Context ctx) {
        try {
            Long customerId = Long.parseLong(ctx.pathParam("id"));
            
            Optional<Customer> customerOpt = customerDAO.getById(customerId);
            
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

            boolean isSerialValidAndEmail = serialLinkService.verifySerialNumberAndEmail(dto.serialNumber,dto.email);
            if (!isSerialValidAndEmail) {
                ErrorResponse.forbidden(ctx, "Invalid serial number is already used and Email: ");
                return;
            }

            // 4. Get the Plan for this serial number
            Plan plan = serialLinkService.getPlanForSerialNumber(dto.serialNumber);
            if (plan == null) {
                ErrorResponse.internalError(ctx, "No plan found for serial number: " + dto.serialNumber);
                return;
            }

            // 5. Get SerialLink to fetch external_customer_id
            dat.entities.SerialLink serialLink = serialLinkService.getSerialLink(dto.serialNumber);
            if (serialLink == null) {
                ErrorResponse.internalError(ctx, "SerialLink not found for serial number: " + dto.serialNumber);
                return;
            }

            // 6. Get User
            User user = securityDAO.getUserByEmail(dto.email);
            if (user == null) {
                ErrorResponse.badRequest(ctx, "No user found with email: " + dto.email);
                return;
            }

            // 7. Check if customer already exists
            if(customerDAO.getByUserEmail(dto.email).isPresent()){
                ErrorResponse.conflict(ctx, "Customer already exists with email: " + dto.email);
                return;
            }

            // 8. Create Customer
            Customer customer = new Customer(
                user, 
                dto.companyName, 
                dto.serialNumber, 
                serialLink.getExternalCustomerId(),
                DateTimeUtil.now()
            );
            Customer savedCustomer = customerDAO.create(customer);
            
            // 9. Create Subscription (using data from external system via SerialLink)
            Subscription subscription = new Subscription(
                savedCustomer,
                plan,
                SubscriptionStatus.ACTIVE,  // Already subscribed in external system
                DateTimeUtil.now(),
                serialLink.getNextPaymentDate(),  // From external system
                AnchorPolicy.ANNIVERSARY
            );
            subscriptionDAO.create(subscription);
            
            // 10. Create SmsBalance (from external SMS provider)
            SmsBalance smsBalance = new SmsBalance(
                savedCustomer.getExternalCustomerId(), 
                serialLink.getInitialSmsBalance()
            );
            smsBalanceDAO.create(smsBalance);
            
            // 11. Convert Entity → DTO for response
            CustomerDTO responseDto = new CustomerDTO();
            responseDto.id = savedCustomer.getId();
            responseDto.email = savedCustomer.getUser().getEmail();
            responseDto.companyName = savedCustomer.getCompanyName();
            responseDto.serialNumber = savedCustomer.getSerialNumber();
            responseDto.externalCustomerId = savedCustomer.getExternalCustomerId();
            responseDto.createdAt = savedCustomer.getCreatedAt();
            
            // 12. Return DTO response
            ctx.status(201);
            ctx.json(responseDto);

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
            
            // Get customer to find their external_customer_id
            Optional<Customer> customerOpt = customerDAO.getById(customerId);
            if (customerOpt.isEmpty()) {
                ErrorResponse.notFound(ctx, "Customer not found");
                return;
            }
            
            Customer customer = customerOpt.get();
            
            // Get SMS balance by external_customer_id
            Optional<SmsBalance> balance = smsBalanceDAO.getByExternalCustomerId(customer.getExternalCustomerId());
            
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
        } catch (Exception e) {
            ErrorResponse.internalError(ctx, "Error fetching SMS balance", logger, e);
        }
    }

}
