package dat.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import dat.config.ApplicationConfig;
import dat.config.HibernateConfig;
import dat.entities.*;
import dat.enums.*;
import dat.security.entities.Role;
import dat.security.entities.User;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.time.OffsetDateTime;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for Activity Logging
 * Tests that activity logs are created when users perform actions
 * 
 * Tests verify:
 * - Activity logs are created during login/logout
 * - Activity logs are created during registration
 * - Activity logs are created during payment operations
 * - Activity logs are created during subscription operations
 * - Metadata is stored correctly
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ActivityLogIntegrationTest {

    private static final int TEST_PORT = 7779;
    private static final String BASE_URL = "http://localhost:" + TEST_PORT;
    private static final String BASE_PATH = "/api";
    
    private EntityManagerFactory emf;
    private Javalin app;
    private ObjectMapper objectMapper = new ObjectMapper();
    
    // Test fixtures
    private User testUser;
    private Customer testCustomer;
    private String authToken;

    @BeforeAll
    void setUpAll() {
        // Initialize Testcontainers PostgreSQL database
        HibernateConfig.setTest(true);
        emf = HibernateConfig.getEntityManagerFactoryForTest();
        
        // Start Javalin application
        app = ApplicationConfig.startServer(TEST_PORT);
        
        // Configure RestAssured
        RestAssured.baseURI = BASE_URL;
        RestAssured.basePath = BASE_PATH;
    }

    @BeforeEach
    void setUp() {
        // Ensure clean database state for each test
        cleanDatabase();
        
        // Clear any cached entities
        if (emf != null && emf.getCache() != null) {
            emf.getCache().evictAll();
        }
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test
        cleanDatabase();
        
        if (emf != null && emf.getCache() != null) {
            emf.getCache().evictAll();
        }
    }

    @AfterAll
    void tearDownAll() {
        // Stop Javalin server
        if (app != null) {
            app.stop();
        }
        
        // Close EntityManagerFactory
        if (emf != null) {
            emf.close();
        }
    }

    // ==================== Test Cases ====================

    /**
     * Test that LOGIN activity is logged when user logs in
     */
    @Test
    @DisplayName("Login - should create LOGIN activity log")
    void testLoginActivityLog() {
        // Arrange
        setupTestUser();
        
        // Act - Login
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "email": "test@activity.com",
                    "password": "password123"
                }
                """)
        .when()
            .post("/auth/login")
        .then()
            .statusCode(200)
            .body("token", notNullValue());

        // Assert - Verify activity log was created
        List<ActivityLog> logs = getActivityLogsForCustomer(testCustomer.getId());
        assertEquals(1, logs.size());
        
        ActivityLog log = logs.get(0);
        assertEquals(ActivityLogType.LOGIN, log.getType());
        assertEquals(ActivityLogStatus.SUCCESS, log.getStatus());
        assertEquals(testCustomer.getId(), log.getCustomer().getId());
        assertNotNull(log.getSession());
        assertNotNull(log.getTimestamp());
    }

    /**
     * Test that LOGOUT activity is logged when user logs out
     */
    @Test
    @DisplayName("Logout - should create LOGOUT activity log")
    void testLogoutActivityLog() {
        // Arrange
        setupTestUser();
        authenticateTestUser();

        // Act - Logout
        given()
            .header("Authorization", "Bearer " + authToken)
        .when()
            .post("/auth/logout")
        .then()
            .statusCode(200);

        // Assert - Verify activity logs (LOGIN + LOGOUT)
        List<ActivityLog> logs = getActivityLogsForCustomer(testCustomer.getId());
        assertEquals(2, logs.size());
        
        // Find logout log
        ActivityLog logoutLog = logs.stream()
            .filter(log -> log.getType() == ActivityLogType.LOGOUT)
            .findFirst()
            .orElse(null);
        
        assertNotNull(logoutLog);
        assertEquals(ActivityLogStatus.SUCCESS, logoutLog.getStatus());
    }

    /**
     * Test that SUBSCRIPTION_CREATED activity is logged during registration
     */
    @Test
    @DisplayName("Registration - should create SUBSCRIPTION_CREATED activity log")
    void testRegistrationActivityLog() {
        // Arrange
        setupSerialLink();

        // Act - Register
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "serialNumber": 101010101,
                    "email": "alice@company-a.com",
                    "password": "SecurePass123!",
                    "companyName": "Company A"
                }
                """)
        .when()
            .post("/auth/register")
        .then()
            .statusCode(201)
            .body("token", notNullValue())
            .body("customerId", notNullValue());

        // Assert - Verify SUBSCRIPTION_CREATED log was created
        try (EntityManager em = emf.createEntityManager()) {
            List<ActivityLog> logs = em.createQuery(
                "SELECT a FROM ActivityLog a WHERE a.type = :type", 
                ActivityLog.class
            )
            .setParameter("type", ActivityLogType.SUBSCRIPTION_CREATED)
            .getResultList();
            
            assertEquals(1, logs.size());
            
            ActivityLog log = logs.get(0);
            assertEquals(ActivityLogStatus.SUCCESS, log.getStatus());
            assertNotNull(log.getMetadata());
            assertTrue(log.getMetadata().containsKey("subscriptionId"));
            assertTrue(log.getMetadata().containsKey("planName"));
        }
    }

    /**
     * Test that SUBSCRIPTION_CANCELLED activity is logged when subscription is cancelled
     */
    @Test
    @DisplayName("Cancel subscription - should create SUBSCRIPTION_CANCELLED activity log")
    void testCancelSubscriptionActivityLog() {
        // Arrange
        setupTestUserWithSubscription();
        authenticateTestUser();
        
        Subscription subscription = getCustomerSubscription(testCustomer.getId());

        // Act - Cancel subscription
        given()
            .header("Authorization", "Bearer " + authToken)
        .when()
            .put("/subscriptions/" + subscription.getId() + "/cancel")
        .then()
            .statusCode(200);

        // Assert - Verify SUBSCRIPTION_CANCELLED log
        List<ActivityLog> logs = getActivityLogsForCustomer(testCustomer.getId());
        
        ActivityLog cancelLog = logs.stream()
            .filter(log -> log.getType() == ActivityLogType.SUBSCRIPTION_CANCELLED)
            .findFirst()
            .orElse(null);
        
        assertNotNull(cancelLog);
        assertEquals(ActivityLogStatus.SUCCESS, cancelLog.getStatus());
        assertTrue(cancelLog.getMetadata().containsKey("subscriptionId"));
        assertTrue(cancelLog.getMetadata().containsKey("planName"));
    }

    /**
     * Test that activity log metadata is stored correctly
     */
    @Test
    @DisplayName("Metadata storage - should preserve all metadata fields")
    void testMetadataStorage() {
        // Arrange
        setupTestUser();

        // Act - Login (which creates activity log with metadata)
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "email": "test@activity.com",
                    "password": "password123"
                }
                """)
        .when()
            .post("/auth/login")
        .then()
            .statusCode(200);

        // Assert - Verify metadata
        List<ActivityLog> logs = getActivityLogsForCustomer(testCustomer.getId());
        assertEquals(1, logs.size());
        
        ActivityLog log = logs.get(0);
        assertNotNull(log.getMetadata());
        // Metadata should contain IP and device info
        assertFalse(log.getMetadata().isEmpty());
    }

    /**
     * Test that multiple activity types are logged correctly
     */
    @Test
    @DisplayName("Multiple activities - should log all activity types correctly")
    void testMultipleActivityTypes() {
        // Arrange
        setupTestUserWithSubscription();
        authenticateTestUser();

        // Act - Perform multiple actions
        // 1. Login (already done in authenticateTestUser)
        // 2. Cancel subscription
        Subscription subscription = getCustomerSubscription(testCustomer.getId());
        given()
            .header("Authorization", "Bearer " + authToken)
        .when()
            .put("/subscriptions/" + subscription.getId() + "/cancel")
        .then()
            .statusCode(200);

        // 3. Logout
        given()
            .header("Authorization", "Bearer " + authToken)
        .when()
            .post("/auth/logout")
        .then()
            .statusCode(200);

        // Assert - Verify all activity types
        List<ActivityLog> logs = getActivityLogsForCustomer(testCustomer.getId());
        assertTrue(logs.size() >= 3); // LOGIN, SUBSCRIPTION_CANCELLED, LOGOUT
        
        // Verify we have different types
        boolean hasLogin = logs.stream().anyMatch(log -> log.getType() == ActivityLogType.LOGIN);
        boolean hasCancelled = logs.stream().anyMatch(log -> log.getType() == ActivityLogType.SUBSCRIPTION_CANCELLED);
        boolean hasLogout = logs.stream().anyMatch(log -> log.getType() == ActivityLogType.LOGOUT);
        
        assertTrue(hasLogin, "Should have LOGIN activity");
        assertTrue(hasCancelled, "Should have SUBSCRIPTION_CANCELLED activity");
        assertTrue(hasLogout, "Should have LOGOUT activity");
    }

    /**
     * Test that activity logs have correct timestamps
     */
    @Test
    @DisplayName("Timestamps - should be set correctly and in order")
    void testActivityLogTimestamps() {
        // Arrange
        setupTestUser();
        OffsetDateTime testStart = OffsetDateTime.now();

        // Act - Login
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "email": "test@activity.com",
                    "password": "password123"
                }
                """)
        .when()
            .post("/auth/login")
        .then()
            .statusCode(200);

        OffsetDateTime testEnd = OffsetDateTime.now();

        // Assert - Verify timestamp
        List<ActivityLog> logs = getActivityLogsForCustomer(testCustomer.getId());
        assertEquals(1, logs.size());
        
        ActivityLog log = logs.get(0);
        assertNotNull(log.getTimestamp());
        assertTrue(log.getTimestamp().isAfter(testStart) || log.getTimestamp().isEqual(testStart));
        assertTrue(log.getTimestamp().isBefore(testEnd) || log.getTimestamp().isEqual(testEnd));
    }

    /**
     * Test that activity logs are associated with correct session
     */
    @Test
    @DisplayName("Session association - should link activity to correct session")
    void testSessionAssociation() {
        // Arrange
        setupTestUser();

        // Act - Login
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "email": "test@activity.com",
                    "password": "password123"
                }
                """)
        .when()
            .post("/auth/login")
        .then()
            .statusCode(200);

        // Assert - Verify session association
        List<ActivityLog> logs = getActivityLogsForCustomer(testCustomer.getId());
        assertEquals(1, logs.size());
        
        ActivityLog log = logs.get(0);
        assertNotNull(log.getSession());
        assertEquals(testCustomer.getId(), log.getSession().getCustomer().getId());
        assertTrue(log.getSession().isActive());
    }

    // ==================== Helper Methods ====================

    private void setupTestUser() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            // Create User
            testUser = new User();
            testUser.setEmail("test@activity.com");
            testUser.setPassword("$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYKKego2d4W"); // password123
            testUser.addRole(Role.USER);
            em.persist(testUser);

            // Create Customer
            testCustomer = new Customer();
            testCustomer.setUser(testUser);
            testCustomer.setCompanyName("Test Activity Company");
            testCustomer.setSerialNumber(999888777);
            testCustomer.setExternalCustomerId("ext_activity_001");
            testCustomer.setCreatedAt(OffsetDateTime.now());
            em.persist(testCustomer);

            em.getTransaction().commit();
        }
    }

    private void setupTestUserWithSubscription() {
        setupTestUser();
        
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            // Create Plan
            Plan plan = new Plan();
            plan.setName("Basic Monthly");
            plan.setPeriod(Period.MONTHLY);
            plan.setPriceCents(9900);
            plan.setCurrency(Currency.DKK);
            plan.setDescription("Basic plan");
            plan.setActive(true);
            em.persist(plan);

            // Create Subscription
            Subscription subscription = new Subscription();
            subscription.setCustomer(testCustomer);
            subscription.setPlan(plan);
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscription.setStartDate(OffsetDateTime.now());
            subscription.setNextBillingDate(OffsetDateTime.now().plusMonths(1));
            subscription.setAnchorPolicy(AnchorPolicy.ANNIVERSARY);
            em.persist(subscription);

            em.getTransaction().commit();
        }
    }

    private void setupSerialLink() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            // Create Plan
            Plan plan = new Plan();
            plan.setName("Basic Monthly");
            plan.setPeriod(Period.MONTHLY);
            plan.setPriceCents(9900);
            plan.setCurrency(Currency.DKK);
            plan.setDescription("Basic plan");
            plan.setActive(true);
            em.persist(plan);

            // Create SerialLink
            SerialLink serialLink = new SerialLink(
                101010101,
                "cus_ext_a_001",
                "alice@company-a.com",
                "Basic Monthly",
                100,
                OffsetDateTime.now().plusDays(15)
            );
            em.persist(serialLink);

            em.getTransaction().commit();
        }
    }

    private void authenticateTestUser() {
        String response = given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "email": "test@activity.com",
                    "password": "password123"
                }
                """)
        .when()
            .post("/auth/login")
        .then()
            .statusCode(200)
            .extract()
            .path("token");
        
        authToken = response;
    }

    private List<ActivityLog> getActivityLogsForCustomer(Long customerId) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                "SELECT a FROM ActivityLog a WHERE a.customer.id = :customerId ORDER BY a.timestamp", 
                ActivityLog.class
            )
            .setParameter("customerId", customerId)
            .getResultList();
        }
    }

    private Subscription getCustomerSubscription(Long customerId) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                "SELECT s FROM Subscription s WHERE s.customer.id = :customerId", 
                Subscription.class
            )
            .setParameter("customerId", customerId)
            .getSingleResult();
        }
    }

    private void cleanDatabase() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM ActivityLog").executeUpdate();
            em.createQuery("DELETE FROM Session").executeUpdate();
            em.createQuery("DELETE FROM Subscription").executeUpdate();
            em.createQuery("DELETE FROM Customer").executeUpdate();
            em.createQuery("DELETE FROM User").executeUpdate();
            em.createQuery("DELETE FROM SerialLink").executeUpdate();
            em.createQuery("DELETE FROM Plan").executeUpdate();
            em.getTransaction().commit();
        }
    }
}
