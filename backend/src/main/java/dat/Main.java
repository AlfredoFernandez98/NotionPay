package dat;

import dat.config.ApplicationConfig;
import dat.config.HibernateConfig;
import dat.mockdatabase.BulkCustomerMigration;
import dat.mockdatabase.SerialLinkMigration;
import dat.services.SessionCleanupService;
import io.javalin.Javalin;
import jakarta.persistence.EntityManagerFactory;


/**
 * Main application entry point
 * Initializes database with test data and starts the API server
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("Initializing NotionPay Backend...\n");
        
        // Step 1: Populate basic test data (Plans, SerialLinks for Alice, Bob, etc.)
        System.out.println("Step 1: Populating basic test data...");
        SerialLinkMigration.populateSerialLinksAndPlans();
        
        // Step 2: Add 1000 bulk customers (only if they don't exist)
        System.out.println("\nStep 2: Adding 1000 bulk customers...");
        BulkCustomerMigration.addBulkCustomers();
        
        // Step 3: Print statistics
        BulkCustomerMigration.printStatistics();
        
        System.out.println("\nDatabase migration completed.\n");

        // Step 4: Start Session Cleanup Service
        System.out.println("\nStep 4: Starting session cleanup service...");
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        SessionCleanupService cleanupService = SessionCleanupService.getInstance(emf);
        cleanupService.startPeriodicCleanup();
        System.out.println("Session cleanup service started (runs every 60 minutes)");
        
        // Step 5: Start server
        System.out.println("\nStep 5: Starting Javalin server on port 7070...");
        Javalin app = ApplicationConfig.startServer(7070);
        System.out.println("Server started successfully.");
        System.out.println("API available at: http://localhost:7070/api");
        System.out.println("Ready to accept requests!\n");
        
        // Graceful shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down NotionPay...");
            cleanupService.shutdown();
            app.stop();
            System.out.println("Shutdown complete. Goodbye!");
        }));
    }
}
