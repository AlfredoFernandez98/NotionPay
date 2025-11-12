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
import dat.dtos.RegisterRequest;
import dat.entities.Plan;
import dat.security.daos.ISecurityDAO;
import dat.security.daos.SecurityDAO;
import dat.security.dtos.UserDTO;
import dat.security.entities.User;
import dat.security.exceptions.ApiException;
import dat.security.exceptions.ValidationException;
// TODO: Add SerialLinkVerificationService when implementing CustomerDAO
// import dat.services.SerialLinkVerificationService;
import dat.services.SerialLinkVerificationService;
import dat.utils.Utils;
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
    
    // TODO: Add these when implementing SerialLink integration (see SERIAL_LINK_INTEGRATION_GUIDE.md)
    // private SerialLinkVerificationService serialLinkService;
    // private CustomerDAO customerDAO;

    private SecurityController() { }

    public static SecurityController getInstance() { // Singleton because we don't want multiple instances of the same class
        if (instance == null) {
            instance = new SecurityController();
        }
        securityDAO = new SecurityDAO(HibernateConfig.getEntityManagerFactory());
        // TODO: Initialize SerialLinkVerificationService and CustomerDAO here
        // instance.serialLinkService = new SerialLinkVerificationService(HibernateConfig.getEntityManagerFactory());
        // instance.customerDAO = new CustomerDAO(HibernateConfig.getEntityManagerFactory());
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
                SerialLinkVerificationService serialLinkVerificationService = ctx.bodyAsClass(SerialLinkVerificationService.class);

                boolean isValid = serialLinkVerificationService.verifySerialNumber(registerRequest.serialNumber);
                if (!isValid){
                    ctx.status(HttpStatus.UNPROCESSABLE_CONTENT);
                    ctx.json(returnObject.put("msg", "Invalid Serial Number"));
                    return;
                }
                Plan eligiblePlan = serialLinkVerificationService.getPlanForSerialNumber(registerRequest.serialNumber);

                
                // Create user (no verification for now)
                User created = securityDAO.createUser(registerRequest.email, registerRequest.password);
                
                // Create JWT token
                String token = createToken(new UserDTO(created.getEmail(), Set.of("USER")));
                ctx.status(HttpStatus.CREATED).json(returnObject
                        .put("token", token)
                        .put("email", created.getEmail())
                        .put("msg", "Registration successful - TODO: Add SerialLink verification"));
                        
            } catch (EntityExistsException e) {
                ctx.status(HttpStatus.UNPROCESSABLE_CONTENT);
                ctx.json(returnObject.put("msg", "User already exists"));
            } catch (Exception e) {
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
                ctx.json(returnObject.put("msg", "Registration failed: " + e.getMessage()));
                logger.error("Registration error: ", e);
            }
        };
    }

    @Override
    public Handler authenticate() throws UnauthorizedResponse {

        ObjectNode returnObject = objectMapper.createObjectNode();
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
                User updatedUser = securityDAO.addRole(user, newRole);
                ctx.status(200).json(returnObject.put("msg", "Role " + newRole + " added to user"));
            } catch (EntityNotFoundException e) {
                ctx.status(404).json("{\"msg\": \"User not found\"}");


                
            }
        };
    }

}