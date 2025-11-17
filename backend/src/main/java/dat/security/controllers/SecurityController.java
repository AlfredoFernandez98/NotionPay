package dat.security.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import dat.config.HibernateConfig;
import dat.daos.impl.CustomerDAO;
import dat.daos.impl.SmsBalanceDAO;
import dat.daos.impl.SubscriptionDAO;
import dat.dtos.RegisterRequest;
import dat.entities.Customer;
import dat.entities.Plan;
import dat.entities.SerialLink;
import dat.entities.SmsBalance;
import dat.entities.Subscription;
import dat.enums.AnchorPolicy;
import dat.enums.SubscriptionStatus;
import dat.security.daos.ISecurityDAO;
import dat.security.daos.SecurityDAO;
import dat.security.dtos.UserDTO;
import dat.security.entities.User;
import dat.security.exceptions.ApiException;
import dat.security.exceptions.ValidationException;
import dat.services.SerialLinkVerificationService;
import dat.utils.Utils;
import dat.utils.ValidationUtil;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.security.RouteRole;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;



/**
 * Purpose: To handle security in the API
 * Author: Thomas Hartmann
 */
public class SecurityController implements ISecurityController {
    ObjectMapper objectMapper = new ObjectMapper();
    private static ISecurityDAO securityDAO;
    private static SecurityController instance;
    private static Logger logger = LoggerFactory.getLogger(SecurityController.class);
    
    private SerialLinkVerificationService serialLinkService;
    private CustomerDAO customerDAO;
    private SubscriptionDAO subscriptionDAO;
    private SmsBalanceDAO smsBalanceDAO;

    private SecurityController() { }

    public static SecurityController getInstance() {
        if (instance == null) {
            instance = new SecurityController();
        }
        securityDAO = new SecurityDAO(HibernateConfig.getEntityManagerFactory());
        instance.serialLinkService = SerialLinkVerificationService.getInstance(HibernateConfig.getEntityManagerFactory());
        instance.customerDAO = CustomerDAO.getInstance(HibernateConfig.getEntityManagerFactory());
        instance.subscriptionDAO = SubscriptionDAO.getInstance(HibernateConfig.getEntityManagerFactory());
        instance.smsBalanceDAO = SmsBalanceDAO.getInstance(HibernateConfig.getEntityManagerFactory());
        return instance;
    }

    @Override
    public Handler login() {
        return (ctx) -> {
            ObjectNode returnObject = objectMapper.createObjectNode(); // for sending json messages back to the client
            try {
                UserDTO user = ctx.bodyAsClass(UserDTO.class);
                UserDTO verifiedUser = securityDAO.getVerifiedUser(user.getEmail(), user.getPassword());
                String token = createToken(verifiedUser);

                ctx.status(200).json(returnObject
                        .put("token", token)
                        .put("email", verifiedUser.getEmail()));

            } catch (EntityNotFoundException | ValidationException e) {
                ctx.status(401);
                System.out.println(e.getMessage());
                ctx.json(returnObject.put("msg", e.getMessage()));
            }
        };
    }

    @Override
    public Handler register() {
        return (ctx) -> {
            ObjectNode returnObject = objectMapper.createObjectNode();
            try {
                RegisterRequest registerRequest = ctx.bodyAsClass(RegisterRequest.class);

                //Checking
                try{
                    ValidationUtil.isValidEmail(registerRequest.email);
                    ValidationUtil.isValidCompanyName(registerRequest.companyName);
                    ValidationUtil.isStrongPassword(registerRequest.password);
                }catch (IllegalArgumentException | NullPointerException e){
                    ctx.status(400);
                    ctx.json(returnObject.put("msg", e.getMessage()));
                    return;
                }
                if (registerRequest.serialNumber == null) {
                    ctx.status(HttpStatus.BAD_REQUEST);
                    ctx.json(returnObject.put("msg", "Serial number cannot be empty"));
                    return;
                }

                // Verify serial number and email match
                boolean isValid = serialLinkService.verifySerialNumberAndEmail(registerRequest.serialNumber,registerRequest.email);
                if (!isValid) {
                    ctx.status(HttpStatus.FORBIDDEN);
                    ctx.json(returnObject.put("msg", "Serial number and email do not match"));
                    logger.warn("Registration failed: Invalid serial number {}", registerRequest.serialNumber);
                    return;
                }
                
                SerialLink serialLink = serialLinkService.getSerialLink(registerRequest.serialNumber);

                // Create User
                User user = securityDAO.createUser(registerRequest.email, registerRequest.password);
                logger.info("User created: {}", user.getEmail());
                
                // Create Customer (with external_customer_id from SerialLink)
                Customer customer = customerDAO.createCustomer(
                    user, 
                    registerRequest.companyName, 
                    registerRequest.serialNumber
                );
                logger.info("Customer created: {} with ID: {}", customer.getCompanyName(), customer.getId());
                
                // Get Plan for subscription
                Plan plan = serialLinkService.getPlanForSerialNumber(registerRequest.serialNumber);
                
                // Create Subscription with trial period
                Subscription subscription = new Subscription(
                    customer,
                    plan,
                    SubscriptionStatus.TRIALING,
                    java.time.OffsetDateTime.now(),
                    java.time.OffsetDateTime.now().plusMonths(1),
                    AnchorPolicy.ANNIVERSARY
                );
                subscriptionDAO.create(subscription);
                logger.info("Subscription created: {} for {}", plan.getName(), customer.getCompanyName());
                
                // Create SmsBalance linked via external_customer_id
                SmsBalance smsBalance = new SmsBalance(
                    customer.getExternalCustomerId(), 
                    serialLink.getInitialSmsBalance()
                );
                smsBalanceDAO.create(smsBalance);
                logger.info("SMS Balance created: {} credits for external ID: {}", 
                    serialLink.getInitialSmsBalance(), customer.getExternalCustomerId());
                
                // Create JWT token
                String token = createToken(new UserDTO(user.getEmail(), Set.of("USER")));
                
                // Return success response
                ctx.status(HttpStatus.CREATED).json(objectMapper.createObjectNode()
                        .put("token", token)
                        .put("email", user.getEmail())
                        .put("customerId", customer.getId())
                        .put("subscriptionId", subscription.getId())
                        .put("planId", plan.getId())
                        .put("planName", plan.getName())
                        .put("initialSmsCredits", serialLink.getInitialSmsBalance())
                        .put("msg", "Registration successful! You are subscribed to " + plan.getName()));
                        
            } catch (EntityExistsException e) {
                ctx.status(HttpStatus.UNPROCESSABLE_CONTENT);
                ctx.json(returnObject.put("msg", "User with this email already exists"));
                logger.warn("Registration failed: User already exists");
            } catch (Exception e) {
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
                ctx.json(returnObject.put("msg", "Registration failed: " + e.getMessage()));
                logger.error("Registration error: ", e);
            }
        };
    }

    @Override
    public Handler authenticate() throws UnauthorizedResponse {
        return (ctx) -> {
            // This is a preflight request => OK
            if (ctx.method().toString().equals("OPTIONS")) {
                ctx.status(200);
                return;
            }
            String header = ctx.header("Authorization");
            if (header == null) {
                throw new UnauthorizedResponse("Authorization header missing");
            }

            String[] headerParts = header.split(" ");
            if (headerParts.length != 2) {
                throw new UnauthorizedResponse("Authorization header malformed");
            }

            String token = headerParts[1];
            UserDTO verifiedTokenUser = verifyToken(token);

            if (verifiedTokenUser == null) {
                throw new UnauthorizedResponse("Invalid User or Token");
            }
            logger.info("User verified: " + verifiedTokenUser);
            ctx.attribute("user", verifiedTokenUser);
        };
    }

    @Override
    // Check if the user's roles contain any of the allowed roles
    public boolean authorize(UserDTO user, Set<RouteRole> allowedRoles) {
        if (user == null) {
            throw new UnauthorizedResponse("You need to log in, dude!");
        }
        Set<String> roleNames = allowedRoles.stream()
                   .map(RouteRole::toString)  // Convert RouteRoles to  Set of Strings
                   .collect(Collectors.toSet());
        return user.getRoles().stream()
                   .map(String::toUpperCase)
                   .anyMatch(roleNames::contains);
        }

    @Override
    public String createToken(UserDTO user) {
        try {
            String ISSUER;
            String TOKEN_EXPIRE_TIME;
            String SECRET_KEY;

            if (System.getenv("DEPLOYED") != null) {
                ISSUER = System.getenv("ISSUER");
                TOKEN_EXPIRE_TIME = System.getenv("TOKEN_EXPIRE_TIME");
                SECRET_KEY = System.getenv("SECRET_KEY");
            } else {
                ISSUER = Utils.getPropertyValue("ISSUER", "config.properties");
                TOKEN_EXPIRE_TIME = Utils.getPropertyValue("TOKEN_EXPIRE_TIME", "config.properties");
                SECRET_KEY = Utils.getPropertyValue("SECRET_KEY", "config.properties");
            }
            
            JWSSigner signer = new MACSigner(SECRET_KEY);
            
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(user.getEmail())
                    .issuer(ISSUER)
                    .claim("email", user.getEmail())
                    .claim("roles", String.join(",", user.getRoles()))
                    .expirationTime(new Date(new Date().getTime() + Long.parseLong(TOKEN_EXPIRE_TIME)))
                    .build();
            
            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
            signedJWT.sign(signer);
            
            return signedJWT.serialize();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiException(500, "Could not create token");
        }
    }

    @Override
    public UserDTO verifyToken(String token) {
        boolean IS_DEPLOYED = (System.getenv("DEPLOYED") != null);
        String SECRET = IS_DEPLOYED ? System.getenv("SECRET_KEY") : Utils.getPropertyValue("SECRET_KEY", "config.properties");

        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            
            // Verify signature
            if (!signedJWT.verify(new MACVerifier(SECRET))) {
                throw new ApiException(403, "Token signature is not valid");
            }
            
            // Check expiration
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            if (new Date().after(claims.getExpirationTime())) {
                throw new ApiException(403, "Token has expired");
            }
            
            // Extract user info
            String email = claims.getStringClaim("email");
            String rolesString = claims.getStringClaim("roles");
            Set<String> roles = Stream.of(rolesString.split(","))
                    .collect(Collectors.toSet());
            
            return new UserDTO(email, roles);
            
        } catch (ParseException | JOSEException e) {
            e.printStackTrace();
            throw new ApiException(HttpStatus.UNAUTHORIZED.getCode(), "Unauthorized. Could not verify token");
        }
    }

    public @NotNull Handler addRole() {
        return (ctx) -> {
            ObjectNode returnObject = objectMapper.createObjectNode();
            try {
                // get the role from the body. the json is {"role": "manager"}.
                // We need to get the role from the body and the email from the token
                String newRole = ctx.bodyAsClass(ObjectNode.class).get("role").asText();
                UserDTO user = ctx.attribute("user");
                securityDAO.addRole(user, newRole);
                ctx.status(200).json(returnObject.put("msg", "Role " + newRole + " added to user"));
            } catch (EntityNotFoundException e) {
                ctx.status(404).json("{\"msg\": \"User not found\"}");
            }
        };
    }

}