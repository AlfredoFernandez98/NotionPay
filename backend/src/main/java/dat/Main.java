package dat;

import dat.config.ApplicationConfig;
import dat.mockdatabase.SerialLinkMigration;
import io.javalin.Javalin;


/**
 * Main class for NotionPay Application
 * Populates mock database with SerialLinks and Plans
 * Starts Javalin server
 */
public class Main {
    public static void main(String[] args) {

        System.out.println("\n" + "=".repeat(60));
        System.out.println("🗄️  MOCK DATABASE MIGRATION - SerialLink + Plans");
        System.out.println("=".repeat(60));

        // Populate SerialLinks with associated Plans
        SerialLinkMigration.populateSerialLinksAndPlans();


        System.out.println("\n" + "=".repeat(60));
        System.out.println("✅ Migration completed successfully!");
        System.out.println("=".repeat(60));



        // Start the server
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🚀 Starting Javalin Server...");
        System.out.println("=".repeat(60));


        Javalin app = ApplicationConfig.startServer(7070);

        System.out.println("\n" + "=".repeat(60));
        System.out.println("✅ Server started successfully on port 7070!");
        System.out.println("📡 API Base URL: http://localhost:7070/api");
        System.out.println("📋 Routes available at: http://localhost:7070/api/routes");
        System.out.println("=".repeat(60));
        System.out.println("\n💡 You can now test the API:");
        System.out.println("   - Register: POST /api/auth/register");
        System.out.println("   - Create Customer: POST /api/customers/");
        System.out.println("   - Use customer.http to test the API\n");

    

        System.out.println("=".repeat(60));
    }
}
