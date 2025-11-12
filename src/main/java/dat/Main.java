package dat;

import dat.mockdatabase.SerialLinkMigration;

/**
 * Main class for NotionPay Application
 * Populates mock database with SerialLinks and Plans
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
        System.out.println("\n💡 You can now test registration with these serial numbers");
        System.out.println("   Each serial is linked to a specific Plan!");
        System.out.println("   Use demoSecurity.http to test the API\n");
    }
}
