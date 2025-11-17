package dat;

import dat.config.ApplicationConfig;
import dat.mockdatabase.SerialLinkMigration;
import io.javalin.Javalin;


/**
 * Main application entry point
 * Initializes database with test data and starts the API server
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("Initializing database migration...");
        SerialLinkMigration.populateSerialLinksAndPlans();
        System.out.println("Migration completed.\n");

        System.out.println("Starting Javalin server on port 7070...");
        Javalin app = ApplicationConfig.startServer(7070);
        System.out.println("Server started successfully.");
        System.out.println("API available at: http://localhost:7070/api");
    }
}
