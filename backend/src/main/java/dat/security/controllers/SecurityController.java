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
import dat.daos.impl.*;
import dat.dtos.RegisterRequest;
import dat.entities.*;
import dat.enums.ActivityLogStatus;
import dat.enums.ActivityLogType;
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
import java.util.*;
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
    private SessionDAO sessionDAO;
    private CustomerDAO customerDAO;
    private ActivityLogDAO activityLogDAO;
    private SubscriptionDAO subscriptionDAO;
    private SmsBalanceDAO smsBalanceDAO;

    private SecurityController() { }

    public static SecurityController getInstance() {
        if (instance == null) {
            instance = new SecurityController();
        }
        securityDAO = new SecurityDAO(HibernateConfig.getEntityManagerFactory());
        instance.serialLinkService = SerialLinkVerificationService.getInstance(HibernateConfig.getEntityManagerFactory());
        instance.sessionDAO = SessionDAO.getInstance(HibernateConfig.getEntityManagerFactory());
        instance.customerDAO = CustomerDAO.getInstance(HibernateConfig.getEntityManagerFactory());
        instance.activityLogDAO = ActivityLogDAO.getInstance(HibernateConfig.getEntityManagerFactory());
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

                Customer customer = customerDAO.getByUserEmail(verifiedUser.getEmail())
                      .orElseThrow(()-> new EntityNotFoundException("Customer with email " + verifiedUser.getEmail() + " not found"));


                var expiresAt= java.time.OffsetDateTime.now().plusHours(2);

                String ip = ctx.req().getRemoteAddr();
                String userAgent =  ctx.header("User-Agent");
                if(userAgent == null){
                    userAgent = "unknown";
                }

                Session session = new Session(customer,token,expiresAt,ip,userAgent);
                sessionDAO.create(session);
                Map<String, Object> metadata = Map.of(
                        "ip"+ ctx.ip(),
                        "device" + userAgent
                );
                ActivityLog activityLog = new ActivityLog(
                        customer,
                        session,
                        ActivityLogType.LOGIN,
                        ActivityLogStatus.SUCCESS,
                        metadata
                );
                activityLogDAO.create(activityLog);
                ctx.status(200).json(returnObject
                        .put("token", token)
                        .put("email", verifiedUser.getEmail())
                        .put("sessionID", session.getId()));

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
                
                // Create Subscription with ACTIVE status (customer already subscribed in external system)
                Subscription subscription = new Subscription(
                    customer,
                    plan,
                    SubscriptionStatus.ACTIVE,
                    java.time.OffsetDateTime.now(),
                    serialLink.getNextPaymentDate(),
                    AnchorPolicy.ANNIVERSARY
                );
                subscriptionDAO.create(subscription);
                logger.info("Subscription created: {} for {} with next payment on {}", 
                    plan.getName(), customer.getCompanyName(), serialLink.getNextPaymentDate());
                
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
                
                // Create Session for activity logging
                java.time.OffsetDateTime expiresAt = java.time.OffsetDateTime.now().plusHours(24);
                String ip = ctx.ip();
                String userAgent = ctx.header("User-Agent");
                if (userAgent == null) {
                    userAgent = "unknown";
                }
                Session session = new Session(customer, token, expiresAt, ip, userAgent);
                sessionDAO.create(session);
                
                // Log subscription creation activity
                Map<String, Object> subscriptionMetadata = new HashMap<>();
                subscriptionMetadata.put("subscriptionId", subscription.getId());
                subscriptionMetadata.put("planId", plan.getId());
                subscriptionMetadata.put("planName", plan.getName());
                subscriptionMetadata.put("startDate", subscription.getStartDate().toString());
                subscriptionMetadata.put("nextBillingDate", subscription.getNextBillingDate().toString());
                
                ActivityLog subscriptionLog = new ActivityLog(
                    customer,
                    session,
                    ActivityLogType.SUBSCRIPTION_CREATED,
                    ActivityLogStatus.SUCCESS,
                    subscriptionMetadata
                );
                activityLogDAO.create(subscriptionLog);
                
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

    /**
     * PURPOSE: Validate an existing JWT token and verify the session exists in database
     * 
     * ENDPOINT: POST /api/auth/validate
     * AUTHORIZATION: ANYONE (no authentication required to validate a token)
     * 
     * REQUEST: Must include Authorization header with Bearer token
     *   Example: Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
     * 
     * RESPONSE on SUCCESS (200):
     *   {
     *     "msg": "Token is valid",
     *     "email": "alice@company-a.com",
     *     "sessionID": 123,
     *     "expiresAt": "2025-11-19T15:30:45+02:00"
     *   }
     * 
     * RESPONSE on FAILURE (401):
     *   - "Authorization header missing" → No Bearer token provided
     *   - "Authorization header malformed" → Wrong format (not "Bearer TOKEN")
     *   - "Invalid token" → Token signature is invalid or expired
     *   - "Session not found" → Token exists but no session in database
     *   - "Session is inactive" → Session was deactivated
     *   - "Session has expired" → Token expiry time has passed
     * 
     * USE CASES:
     *   1. Frontend: Check if user is still logged in
     *   2. Frontend: Before making API calls, validate token is still valid
     *   3. Logout: Find session by token to deactivate it
     *   4. Testing: Verify sessions are created after login
     *   5. Monitoring: Check active sessions in the system
     * 
     * IMPORTANT: This endpoint does NOT require the authenticate() middleware
     *            because it validates the token itself
     */
    @Override
    public Handler validate() {
        return (ctx) -> {
            ObjectNode returnObject = objectMapper.createObjectNode();
            try {
                // Step 1: Extract Authorization header
                String header = ctx.header("Authorization");
                if (header == null) {
                    ctx.status(401).json(returnObject.put("msg", "Authorization header missing"));
                    return;
                }

                // Step 2: Parse "Bearer TOKEN" format
                String[] headerParts = header.split(" ");
                if (headerParts.length != 2) {
                    ctx.status(401).json(returnObject.put("msg", "Authorization header malformed"));
                    return;
                }

                // Step 3: Extract token and verify JWT signature & expiration
                String token = headerParts[1];
                UserDTO verifiedUser = verifyToken(token);

                if (verifiedUser == null) {
                    ctx.status(401).json(returnObject.put("msg", "Invalid token"));
                    return;
                }

                // Step 4: Check if session exists in database (extra security layer)
                Optional<Session> session = sessionDAO.findByToken(token);
                if (session.isEmpty()) {
                    ctx.status(401).json(returnObject.put("msg", "Session not found"));
                    return;
                }

                // Step 5: Verify session is still active (not manually deactivated)
                if (!session.get().getActive()) {
                    ctx.status(401).json(returnObject.put("msg", "Session is inactive"));
                    return;
                }

                // Step 6: Verify session has not expired (double-check with database)
                if (java.time.OffsetDateTime.now().isAfter(session.get().getExpiresAt())) {
                    ctx.status(401).json(returnObject.put("msg", "Session has expired"));
                    return;
                }

                // Step 7: All checks passed! Return success with session details
                ctx.status(200).json(returnObject
                        .put("msg", "Token is valid")
                        .put("email", verifiedUser.getEmail())
                        .put("sessionID", session.get().getId())
                        .put("expiresAt", session.get().getExpiresAt().toString()));

            } catch (Exception e) {
                // If anything unexpected happens, return 401 Unauthorized
                ctx.status(401);
                logger.error("Token validation failed: " + e.getMessage());
                ctx.json(returnObject.put("msg", "Token validation failed: " + e.getMessage()));
            }
        };
    }

    @Override
    public Handler logout() {
        return (ctx) -> {
            ObjectNode returnObject = objectMapper.createObjectNode();
            try {
                // Extract token from Authorization header
                String header = ctx.header("Authorization");
                if (header == null || !header.startsWith("Bearer ")) {
                    ctx.status(401).json(returnObject.put("msg", "Authorization header missing"));
                    return;
                }

                String token = header.substring(7);

                // Find session by token
                Optional<Session> sessionOpt = sessionDAO.findByToken(token);
                if (sessionOpt.isEmpty()) {
                    ctx.status(404).json(returnObject.put("msg", "Session not found"));
                    return;
                }

                Session session = sessionOpt.get();

                // Deactivate session
                session.setActive(false);
                sessionDAO.update(session);

                // Log logout activity
                Customer customer = session.getCustomer();
                ActivityLog activityLog = new ActivityLog(
                        customer,
                        session,
                        ActivityLogType.LOGOUT,
                        ActivityLogStatus.SUCCESS,
                        Map.of("ip", ctx.ip())
                );
                activityLogDAO.create(activityLog);

                ctx.status(200).json(returnObject.put("msg", "Logged out successfully"));

            } catch (Exception e) {
                logger.error("Logout failed: " + e.getMessage());
                ctx.status(500).json(returnObject.put("msg", "Logout failed: " + e.getMessage()));
            }
        };
    }

}