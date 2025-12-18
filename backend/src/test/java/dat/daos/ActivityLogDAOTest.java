package dat.daos;

import dat.config.HibernateConfig;
import dat.daos.impl.ActivityLogDAO;
import dat.daos.impl.CustomerDAO;
import dat.daos.impl.SessionDAO;
import dat.entities.ActivityLog;
import dat.entities.Customer;
import dat.entities.Session;
import dat.enums.ActivityLogStatus;
import dat.enums.ActivityLogType;
import dat.security.entities.Role;
import dat.security.entities.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for ActivityLogDAO
 * Tests core CRUD operations and activity log queries
 */
class ActivityLogDAOTest {

    private static EntityManagerFactory emf;
    private static ActivityLogDAO activityLogDAO;
    private static SessionDAO sessionDAO;
    private static CustomerDAO customerDAO;
    
    // Test data
    private User testUser;
    private Customer testCustomer;
    private Session testSession;
    private ActivityLog testActivityLog;

    @BeforeAll
    static void setUpAll() {
        // Initialize test database with Testcontainers
        HibernateConfig.setTest(true);
        emf = HibernateConfig.getEntityManagerFactoryForTest();
        activityLogDAO = ActivityLogDAO.getInstance(emf);
        sessionDAO = SessionDAO.getInstance(emf);
        customerDAO = CustomerDAO.getInstance(emf);
    }

    @BeforeEach
    void setUp() {
        // Clean database before each test
        cleanDatabase();
        
        // Set up test data
        setupTestData();
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test
        cleanDatabase();
    }

    @AfterAll
    static void tearDownAll() {
        if (emf != null) {
            emf.close();
        }
    }

    /**
     * Test the create() method
     * Verifies that an activity log can be created and persisted to the database
     */
    @Test
    @DisplayName("Create activity log - should persist and return log with generated ID")
    void testCreate() {
        // Arrange
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("action", "test_action");
        metadata.put("details", "test details");
        
        ActivityLog activityLog = new ActivityLog(
            testCustomer,
            testSession,
            ActivityLogType.LOGIN,
            ActivityLogStatus.SUCCESS,
            metadata
        );

        // Act
        ActivityLog created = activityLogDAO.create(activityLog);

        // Assert
        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals(testCustomer.getId(), created.getCustomer().getId());
        assertEquals(testSession.getId(), created.getSession().getId());
        assertEquals(ActivityLogType.LOGIN, created.getType());
        assertEquals(ActivityLogStatus.SUCCESS, created.getStatus());
        assertNotNull(created.getTimestamp());
        assertEquals("test_action", created.getMetadata().get("action"));
    }

    /**
     * Test the getById() method
     * Verifies that an activity log can be retrieved by ID
     */
    @Test
    @DisplayName("Get by ID - should return activity log when exists")
    void testGetById() {
        // Arrange
        ActivityLog created = activityLogDAO.create(testActivityLog);

        // Act
        Optional<ActivityLog> result = activityLogDAO.getById(created.getId());

        // Assert
        assertTrue(result.isPresent());
        assertEquals(created.getId(), result.get().getId());
        assertEquals(ActivityLogType.PAYMENT, result.get().getType());
    }

    /**
     * Test the getById() method with non-existent ID
     */
    @Test
    @DisplayName("Get by ID - should return empty when not exists")
    void testGetByIdNotFound() {
        // Act
        Optional<ActivityLog> result = activityLogDAO.getById(999L);

        // Assert
        assertTrue(result.isEmpty());
    }

    /**
     * Test the getAll() method
     * Verifies that all activity logs can be retrieved
     */
    @Test
    @DisplayName("Get all - should return all activity logs")
    void testGetAll() {
        // Arrange
        ActivityLog log1 = activityLogDAO.create(testActivityLog);
        
        Map<String, Object> metadata2 = new HashMap<>();
        metadata2.put("action", "logout");
        ActivityLog log2 = new ActivityLog(
            testCustomer,
            testSession,
            ActivityLogType.LOGOUT,
            ActivityLogStatus.SUCCESS,
            metadata2
        );
        activityLogDAO.create(log2);

        // Act
        Set<ActivityLog> logs = activityLogDAO.getAll();

        // Assert
        assertNotNull(logs);
        assertEquals(2, logs.size());
    }

    /**
     * Test activity log with different types
     */
    @Test
    @DisplayName("Create logs with different types - should persist all types correctly")
    void testDifferentActivityTypes() {
        // Test LOGIN
        ActivityLog loginLog = new ActivityLog(
            testCustomer, testSession, ActivityLogType.LOGIN, 
            ActivityLogStatus.SUCCESS, new HashMap<>()
        );
        ActivityLog createdLogin = activityLogDAO.create(loginLog);
        assertEquals(ActivityLogType.LOGIN, createdLogin.getType());

        // Test PAYMENT
        ActivityLog paymentLog = new ActivityLog(
            testCustomer, testSession, ActivityLogType.PAYMENT, 
            ActivityLogStatus.SUCCESS, new HashMap<>()
        );
        ActivityLog createdPayment = activityLogDAO.create(paymentLog);
        assertEquals(ActivityLogType.PAYMENT, createdPayment.getType());

        // Test SUBSCRIPTION_CREATED
        ActivityLog subLog = new ActivityLog(
            testCustomer, testSession, ActivityLogType.SUBSCRIPTION_CREATED, 
            ActivityLogStatus.SUCCESS, new HashMap<>()
        );
        ActivityLog createdSub = activityLogDAO.create(subLog);
        assertEquals(ActivityLogType.SUBSCRIPTION_CREATED, createdSub.getType());

        // Test ADD_CARD
        ActivityLog cardLog = new ActivityLog(
            testCustomer, testSession, ActivityLogType.ADD_CARD, 
            ActivityLogStatus.SUCCESS, new HashMap<>()
        );
        ActivityLog createdCard = activityLogDAO.create(cardLog);
        assertEquals(ActivityLogType.ADD_CARD, createdCard.getType());
    }

    /**
     * Test activity log with SUCCESS and FAILURE status
     */
    @Test
    @DisplayName("Create logs with different statuses - should persist both SUCCESS and FAILURE")
    void testDifferentStatuses() {
        // Test SUCCESS
        ActivityLog successLog = new ActivityLog(
            testCustomer, testSession, ActivityLogType.LOGIN, 
            ActivityLogStatus.SUCCESS, new HashMap<>()
        );
        ActivityLog createdSuccess = activityLogDAO.create(successLog);
        assertEquals(ActivityLogStatus.SUCCESS, createdSuccess.getStatus());

        // Test FAILURE
        ActivityLog failureLog = new ActivityLog(
            testCustomer, testSession, ActivityLogType.PAYMENT, 
            ActivityLogStatus.FAILURE, new HashMap<>()
        );
        ActivityLog createdFailure = activityLogDAO.create(failureLog);
        assertEquals(ActivityLogStatus.FAILURE, createdFailure.getStatus());
    }

    /**
     * Test metadata storage and retrieval
     */
    @Test
    @DisplayName("Store and retrieve metadata - should preserve all metadata fields")
    void testMetadataStorage() {
        // Arrange
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("paymentId", 123L);
        metadata.put("amount", 9900);
        metadata.put("currency", "DKK");
        metadata.put("subscriptionId", 456L);
        metadata.put("ip", "192.168.1.1");
        
        ActivityLog log = new ActivityLog(
            testCustomer, testSession, ActivityLogType.PAYMENT, 
            ActivityLogStatus.SUCCESS, metadata
        );

        // Act
        ActivityLog created = activityLogDAO.create(log);
        Optional<ActivityLog> retrieved = activityLogDAO.getById(created.getId());

        // Assert
        assertTrue(retrieved.isPresent());
        Map<String, Object> retrievedMetadata = retrieved.get().getMetadata();
        assertEquals(123, retrievedMetadata.get("paymentId"));
        assertEquals(9900, retrievedMetadata.get("amount"));
        assertEquals("DKK", retrievedMetadata.get("currency"));
        assertEquals(456, retrievedMetadata.get("subscriptionId"));
        assertEquals("192.168.1.1", retrievedMetadata.get("ip"));
    }

    /**
     * Test timestamp is automatically set
     */
    @Test
    @DisplayName("Timestamp - should be automatically set on creation")
    void testTimestampAutoSet() {
        // Arrange
        OffsetDateTime before = OffsetDateTime.now();
        
        ActivityLog log = new ActivityLog(
            testCustomer, testSession, ActivityLogType.LOGIN, 
            ActivityLogStatus.SUCCESS, new HashMap<>()
        );

        // Act
        ActivityLog created = activityLogDAO.create(log);
        OffsetDateTime after = OffsetDateTime.now();

        // Assert
        assertNotNull(created.getTimestamp());
        assertTrue(created.getTimestamp().isAfter(before) || created.getTimestamp().isEqual(before));
        assertTrue(created.getTimestamp().isBefore(after) || created.getTimestamp().isEqual(after));
    }

    /**
     * Test activity log with complex metadata
     */
    @Test
    @DisplayName("Complex metadata - should handle nested structures")
    void testComplexMetadata() {
        // Arrange
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("action", "subscription_renewal");
        metadata.put("previousDate", "2025-01-15");
        metadata.put("nextDate", "2025-02-15");
        metadata.put("planDetails", Map.of("name", "Pro Plan", "price", 19900));
        
        ActivityLog log = new ActivityLog(
            testCustomer, testSession, ActivityLogType.SUBSCRIPTION_RENEWED, 
            ActivityLogStatus.SUCCESS, metadata
        );

        // Act
        ActivityLog created = activityLogDAO.create(log);
        Optional<ActivityLog> retrieved = activityLogDAO.getById(created.getId());

        // Assert
        assertTrue(retrieved.isPresent());
        Map<String, Object> retrievedMetadata = retrieved.get().getMetadata();
        assertEquals("subscription_renewal", retrievedMetadata.get("action"));
        assertNotNull(retrievedMetadata.get("planDetails"));
    }

    // ==================== Helper Methods ====================

    private void setupTestData() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            // Create User
            testUser = new User();
            testUser.setEmail("test@activitylog.com");
            testUser.setPassword("hashedpassword");
            testUser.addRole(Role.USER);
            em.persist(testUser);

            // Create Customer
            testCustomer = new Customer();
            testCustomer.setUser(testUser);
            testCustomer.setCompanyName("Test Company");
            testCustomer.setSerialNumber(123456789);
            testCustomer.setExternalCustomerId("ext_test_001");
            testCustomer.setCreatedAt(OffsetDateTime.now());
            em.persist(testCustomer);

            // Create Session
            testSession = new Session();
            testSession.setCustomer(testCustomer);
            testSession.setToken("test_token_123");
            testSession.setExpiresAt(OffsetDateTime.now().plusHours(24));
            testSession.setIp("127.0.0.1");
            testSession.setUserAgent("Test Agent");
            testSession.setActive(true);
            em.persist(testSession);

            // Create test ActivityLog
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("paymentId", 1L);
            metadata.put("amount", 9900);
            
            testActivityLog = new ActivityLog(
                testCustomer,
                testSession,
                ActivityLogType.PAYMENT,
                ActivityLogStatus.SUCCESS,
                metadata
            );

            em.getTransaction().commit();
        }
    }

    private void cleanDatabase() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM ActivityLog").executeUpdate();
            em.createQuery("DELETE FROM Session").executeUpdate();
            em.createQuery("DELETE FROM Customer").executeUpdate();
            em.createQuery("DELETE FROM User").executeUpdate();
            em.getTransaction().commit();
        }
    }
}
