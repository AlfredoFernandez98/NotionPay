package dat.services;

import dat.daos.impl.SessionDAO;
import dat.entities.Session;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Service for managing user sessions
 * Handles session retrieval and JWT token operations
 * 
 * @author NotionPay Team
 */
public class SessionService {
    private static SessionService instance;
    private static final Logger logger = LoggerFactory.getLogger(SessionService.class);
    
    private final SessionDAO sessionDAO;

    public static SessionService getInstance(EntityManagerFactory emf) {
        if (instance == null) {
            instance = new SessionService(emf);
        }
        return instance;
    }

    private SessionService(EntityManagerFactory emf) {
        this.sessionDAO = SessionDAO.getInstance(emf);
        logger.info("SessionService initialized");
    }

    /**
     * Get session by JWT token
     * 
     * @param token JWT token
     * @return Optional containing session if found
     */
    public Optional<Session> getByToken(String token) {
        return sessionDAO.findByToken(token);
    }

    /**
     * Extract session from Authorization header
     * 
     * @param authHeader Authorization header (Bearer token)
     * @return Optional containing session if found
     */
    public Optional<Session> getFromAuthHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Optional.empty();
        }
        
        String token = authHeader.substring(7);
        return getByToken(token);
    }
}
