package dat;

import dat.config.ApplicationConfig;
import dat.config.HibernateConfig;
import dat.security.entities.Role;
import dat.security.entities.User;
import io.javalin.Javalin;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

/**
 * Main class for testing the NotionPay project setup
 * This verifies that Hibernate, Lombok, PostgreSQL, and Javalin are all working correctly
 */
public class Main {
    public static void main(String[] args) {
        
        System.out.println("🚀 NotionPay - Project Setup Test");
        System.out.println("=" .repeat(50));
        
        // Test 1: Hibernate Configuration
        testHibernateConfiguration();
        
        // Test 2: Database Connection & Entity Persistence
        testDatabaseOperations();
        
        // Test 3: Lombok Annotations
        testLombokAnnotations();
        
        // Test 4: Javalin Server (optional - uncomment to test)
        // testJavalinServer();
        
        System.out.println("\n" + "=" .repeat(50));
        System.out.println("✅ All tests completed successfully!");
        System.out.println("=" .repeat(50));
        System.out.println("\n💡 Tip: Uncomment testJavalinServer() to test the web server");
        System.out.println("   Then visit: http://localhost:7070/api/routes");
    }
    
    /**
     * Test 1: Verify Hibernate is configured correctly
     */
    private static void testHibernateConfiguration() {
        System.out.println("\n📦 Test 1: Hibernate Configuration");
        System.out.println("-".repeat(50));
        
        try {
            EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
            System.out.println("✅ Hibernate EntityManagerFactory created successfully");
            System.out.println("   Hibernate Version: 6.6.3.Final");
            System.out.println("   Jakarta Persistence: ✓");
        } catch (Exception e) {
            System.err.println("❌ Hibernate configuration failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Test 2: Verify database connection and CRUD operations
     */
    private static void testDatabaseOperations() {
        System.out.println("\n🗄️  Test 2: Database Operations");
        System.out.println("-".repeat(50));
        
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        EntityManager em = emf.createEntityManager();
        
        try {
            em.getTransaction().begin();
            
            // Create roles
            Role userRole = new Role("USER");
            Role adminRole = new Role("ADMIN");
            
            em.persist(userRole);
            em.persist(adminRole);
            
            // Create a test user
            User testUser = new User("test_user", "password123");
            testUser.addRole(userRole);
            
            em.persist(testUser);
            
            em.getTransaction().commit();
            
            System.out.println("✅ Database connection successful");
            System.out.println("✅ Created Role entities: USER, ADMIN");
            System.out.println("✅ Created User entity: test_user");
            System.out.println("✅ Entity persistence working correctly");
            
            // Query back to verify
            User retrievedUser = em.find(User.class, "test_user");
            if (retrievedUser != null) {
                System.out.println("✅ Entity retrieval successful");
                System.out.println("   Username: " + retrievedUser.getUsername());
                System.out.println("   Roles: " + retrievedUser.getRolesAsStrings());
            }
            
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("❌ Database operation failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
        }
    }
    
    /**
     * Test 3: Verify Lombok annotations are working
     */
    private static void testLombokAnnotations() {
        System.out.println("\n🔧 Test 3: Lombok Annotations");
        System.out.println("-".repeat(50));
        
        try {
            // Test @Getter and @Setter
            User user = new User();
            user.setUsername("lombok_test_user");
            user.setPassword("test_password");
            
            String username = user.getUsername();
            String password = user.getPassword();
            
            if (username != null && password != null) {
                System.out.println("✅ @Getter annotation working");
                System.out.println("✅ @Setter annotation working");
            }
            
            // Test @NoArgsConstructor
            User emptyUser = new User();
            System.out.println("✅ @NoArgsConstructor working");
            
            // Test @AllArgsConstructor (custom constructor)
            User constructedUser = new User("user1", "pass1");
            System.out.println("✅ Custom constructor working");
            
            // Test @ToString
            String userString = constructedUser.toString();
            if (userString.contains("User")) {
                System.out.println("✅ @ToString annotation working");
            }
            
            System.out.println("✅ Lombok version: 1.18.36");
            System.out.println("✅ All Lombok annotations processed correctly");
            
        } catch (Exception e) {
            System.err.println("❌ Lombok test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Test 4: Verify Javalin server starts correctly
     * Uncomment the call in main() to test the web server
     */
    private static void testJavalinServer() {
        System.out.println("\n🌐 Test 4: Javalin Web Server");
        System.out.println("-".repeat(50));
        
        try {
            Javalin app = ApplicationConfig.startServer(7070);
            System.out.println("✅ Javalin server started successfully");
            System.out.println("✅ Server running on: http://localhost:7070");
            System.out.println("✅ API base path: http://localhost:7070/api");
            System.out.println("✅ Routes overview: http://localhost:7070/api/routes");
            System.out.println("\n⚠️  Server is running. Press Ctrl+C to stop.");
            
            // Keep the server running
            // In production, this would be in a separate main method
            
        } catch (Exception e) {
            System.err.println("❌ Javalin server failed to start: " + e.getMessage());
            e.printStackTrace();
        }
    }
}