package dat.services;

import dat.daos.impl.SessionDAO;
import dat.entities.Session;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service for cleaning up expired sessions
 * Runs periodically to deactivate old sessions
 * 
 * Security improvement: Prevents session table from growing indefinitely
 * and ensures expired sessions cannot be used
 * 
 * @author NotionPay Team
 */
public class SessionCleanupService {
    private static final Logger logger = LoggerFactory.getLogger(SessionCleanupService.class);
    private static SessionCleanupService instance;
    private final ScheduledExecutorService scheduler;
    private final SessionDAO sessionDAO;
    
    private SessionCleanupService(EntityManagerFactory emf) {
        this.sessionDAO = SessionDAO.getInstance(emf);
        this.scheduler = Executors.newScheduledThreadPool(1);
        logger.info("SessionCleanupService initialized");
    }
    
    public static SessionCleanupService getInstance(EntityManagerFactory emf) {
        if (instance == null) {
            instance = new SessionCleanupService(emf);
        }
        return instance;
    }
    
    /**
     * Start periodic cleanup (runs every hour)
     */
    public void startPeriodicCleanup() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                cleanupExpiredSessions();
            } catch (Exception e) {
                logger.error("Session cleanup failed", e);
            }
        }, 1, 60, TimeUnit.MINUTES); // Run every hour
        
        logger.info("Periodic session cleanup started (every 60 minutes)");
    }
    
    /**
     * Clean up expired sessions
     * Deactivates sessions that have expired
     */
    public int cleanupExpiredSessions() {
        logger.info("Starting session cleanup...");
        
        Set<Session> allSessions = sessionDAO.getAll();
        OffsetDateTime now = OffsetDateTime.now();
        int deactivatedCount = 0;
        
        for (Session session : allSessions) {
            if (session.getActive() && session.getExpiresAt().isBefore(now)) {
                session.setActive(false);
                sessionDAO.update(session);
                deactivatedCount++;
            }
        }
        
        logger.info(" Session cleanup completed: {} sessions deactivated", deactivatedCount);
        return deactivatedCount;
    }
    
    /**
     * Stop the cleanup service
     */
    public void shutdown() {
        scheduler.shutdown();
        logger.info("SessionCleanupService shutdown");
    }
}

