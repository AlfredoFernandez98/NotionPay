package dat.controllers.impl;

import dat.controllers.IController;
import dat.daos.impl.CustomerDAO;
import dat.daos.impl.SmsBalanceDAO;
import dat.dtos.CustomerDTO;
import dat.entities.Customer;
import dat.entities.Plan;
import dat.entities.SmsBalance;
import dat.security.daos.SecurityDAO;
import dat.security.dtos.UserDTO;
import dat.security.entities.User;
import dat.services.SerialLinkVerificationService;
import io.javalin.http.Context;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.Getter;
import org.eclipse.jetty.server.Authentication;

import java.util.Map;
import java.util.Optional;

public class CustomerController implements IController{

    private final CustomerDAO customerDAO;
    private final SmsBalanceDAO smsBalanceDAO;
    private final SerialLinkVerificationService serialLinkService;
    private final EntityManagerFactory emf;


    public CustomerController(EntityManagerFactory emf, SerialLinkVerificationService serialLinkService) {
        this.customerDAO = CustomerDAO.getInstance(emf);
        this.smsBalanceDAO = SmsBalanceDAO.getInstance(emf);
        this.serialLinkService = serialLinkService.getInstance(emf);
        this.emf = emf;


    }
    @Override
    public void read(Context ctx) {

    }

    @Override
    public void readAll(Context ctx) {

    }

    @Override
    public void create(Context ctx) {

        try {
            CustomerDTO dto = ctx.bodyAsClass(CustomerDTO.class);

            if (dto.companyName == null || dto.companyName.isEmpty()) {
                ctx.status(400);
                ctx.json("Customer name cannot be empty");
                return;
            }

            if (dto.email == null || dto.email.isEmpty()) {
                ctx.status(400);
                ctx.json("Customer email cannot be empty");
                return;
            }
            if (dto.serialNumber == null) {
                ctx.status(400);
                ctx.json("Serial number cannot be empty");
                return;
            }

            boolean isSerialValid = serialLinkService.verifySerialNumber(dto.serialNumber);
            if (!isSerialValid) {
                ctx.status(403);
                ctx.json("Invalid serial number is already used: " + dto.serialNumber);
                return;
            }

            // 4. Get the Plan for this serial number
            Plan plan = serialLinkService.getPlanForSerialNumber(dto.serialNumber);
            if (plan == null) {
                ctx.status(500);
                ctx.json("No plan found for serial number: " + dto.serialNumber);
                return;
            }

            // 5. Get full SerialLink (contains external_customer_id)
            dat.entities.SerialLink serialLink = serialLinkService.getSerialLink(dto.serialNumber);
            if (serialLink == null) {
                ctx.status(500);
                ctx.json("SerialLink not found for serial number: " + dto.serialNumber);
                return;
            }

            // 6. Get User from database
            User user = getUserByEmail(dto.email);
            if (user == null) {
                ctx.status(400);
                ctx.json("No user found with email: " + dto.email);
                return;
            }

            // 7. Check if customer already exists for this user
            if(customerDAO.getByUserEmail(dto.email).isPresent()){
                ctx.status(409);
                ctx.json("Customer already exists with email: " + dto.email);
                return;
            }

            // 8. Create Customer with external_customer_id from SerialLink
            Customer customer = new Customer(user, dto.companyName, dto.serialNumber);
            customer.setExternalCustomerId(serialLink.getExternalCustomerId()); // ← FROM EXTERNAL DB!
            Customer savedCustomer = customerDAO.create(customer);

            // 9. Link Customer to SerialLink (marks as VERIFIED)
            serialLinkService.linkCustomerToSerialLink(dto.serialNumber, savedCustomer);
            
            // 10. Return success response
            ctx.status(201);
            ctx.json("customer saved with id: " + savedCustomer.getUser().getEmail() + 
                    " under plan: " + plan.getName() + 
                    " (external_id: " + savedCustomer.getExternalCustomerId() + ")");


            }   catch(Exception e){
                ctx.status(500);
                ctx.json("Internal server error: " + e.getMessage());
            }


        }

        @Override
        public void update (Context ctx){

        }

        @Override
        public void delete (Context ctx){

        }

    public User getUserByEmail (String email){
        try (EntityManager em = emf.createEntityManager()) {
            return em.find(User.class, email);
        }
    }

    /**
     * GET /api/customers/{id}/sms-balance
     * Fetch SMS balance from external SMS provider DB
     */
    

}
