package dat.integration;

import dat.config.ApplicationConfig;
import dat.config.HibernateConfig;
import dat.dtos.CustomerDTO;
import dat.entities.Customer;
import dat.entities.Plan;
import dat.entities.SerialLink;
import dat.entities.SmsBalance;
import dat.entities.Subscription;
import dat.enums.Currency;
import dat.enums.Period;
import dat.enums.SubscriptionStatus;
import dat.security.entities.Role;
import dat.security.entities.User;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test: HTTP → Controller → DAO → Database
 * Tests customer creation with full flow including subscription and SMS balance
 * Uses Testcontainers PostgreSQL (test_db) for isolated testing
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CustomerIntegrationTest {

    private static final int TEST_PORT = 7777;
    private static final String BASE_URL = "http://localhost:" + TEST_PORT;
    private static final String BASE_PATH = "/api";
    
    private EntityManagerFactory emf;
    private Javalin app;
    
    // Test fixtures
    private User testUser;
    private Plan testPlan;
    private SerialLink testSerialLink;

    @BeforeAll
    void setUpAll() {
        // Initialize Testcontainers PostgreSQL database (test_db)
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
        setupTestFixtures();
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test to ensure isolation
        cleanDatabase();
    }

    @AfterAll
    void tearDownAll() {
        // Stop application and close resources
        if (app != null) {
            ApplicationConfig.stopServer(app);
        }
        if (emf != null) {
            emf.close();
        }
    }

    @Test
    @DisplayName("POST /api/customers - Should create customer and return 201")
    void testCreateCustomer_Success() {
        // Arrange
        CustomerDTO requestDto = new CustomerDTO();
        requestDto.email = testUser.getEmail();
        requestDto.companyName = "Tech Solutions AB";
        requestDto.serialNumber = testSerialLink.getSerialNumber();

        // Act: Send HTTP POST request
        given()
            .contentType(ContentType.JSON)
            .body(requestDto)
        .when()
            .post("/customers/")
        .then()
            .statusCode(201)
            .body(containsString("Customer saved"))
            .body(containsString(testUser.getEmail()))
            .body(containsString(testPlan.getName()))
            .body(containsString(testSerialLink.getExternalCustomerId()));

        // Assert: Verify customer persisted in database
        Customer customer = findCustomerByEmailOrFail(testUser.getEmail());
        assertNotNull(customer.getId());
        assertEquals("Tech Solutions AB", customer.getCompanyName());
        assertEquals(testUser.getEmail(), customer.getUser().getEmail());
        assertEquals(testSerialLink.getSerialNumber(), customer.getSerialNumber());
        assertEquals(testSerialLink.getExternalCustomerId(), customer.getExternalCustomerId());
        assertNotNull(customer.getCreatedAt());
    }

    @Test
    @DisplayName("POST /api/customers - Should create subscription with correct plan")
    void testCreateCustomer_CreatesSubscription() {
        // Arrange
        CustomerDTO requestDto = new CustomerDTO();
        requestDto.email = testUser.getEmail();
        requestDto.companyName = "Subscription Test Co";
        requestDto.serialNumber = testSerialLink.getSerialNumber();

        // Act: Create customer
        given()
            .contentType(ContentType.JSON)
            .body(requestDto)
        .when()
            .post("/customers/")
        .then()
            .statusCode(201);

        // Assert: Verify subscription was created with correct values
        Customer customer = findCustomerByEmailOrFail(testUser.getEmail());
        Subscription subscription = findSubscriptionByCustomerIdOrFail(customer.getId());
        
        assertEquals(customer.getId(), subscription.getCustomer().getId());
        assertEquals(testPlan.getId(), subscription.getPlan().getId());
        assertEquals(testPlan.getName(), subscription.getPlan().getName());
        assertEquals(SubscriptionStatus.TRIALING, subscription.getStatus());
        assertNotNull(subscription.getStartDate());
        assertNotNull(subscription.getNextBillingDate());
    }

    @Test
    @DisplayName("POST /api/customers - Should create SMS balance with initial credits")
    void testCreateCustomer_CreatesSmsBalance() {
        // Arrange
        CustomerDTO requestDto = new CustomerDTO();
        requestDto.email = testUser.getEmail();
        requestDto.companyName = "SMS Test Company";
        requestDto.serialNumber = testSerialLink.getSerialNumber();

        // Act: Create customer
        given()
            .contentType(ContentType.JSON)
            .body(requestDto)
        .when()
            .post("/customers/")
        .then()
            .statusCode(201);

        // Assert: Verify SMS balance was created with correct initial balance
        Customer customer = findCustomerByEmailOrFail(testUser.getEmail());
        SmsBalance smsBalance = findSmsBalanceByExternalCustomerIdOrFail(customer.getExternalCustomerId());
        
        assertEquals(testSerialLink.getExternalCustomerId(), smsBalance.getExternalCustomerId());
        assertEquals(testSerialLink.getInitialSmsBalance(), smsBalance.getRemainingSms());
        assertEquals(500, smsBalance.getRemainingSms()); // Explicit value check
        assertNotNull(smsBalance.getId());
    }

    @Test
    @DisplayName("POST /api/customers - Should return 400 when company name is missing")
    void testCreateCustomer_MissingCompanyName() {
        // Arrange
        CustomerDTO requestDto = new CustomerDTO();
        requestDto.email = testUser.getEmail();
        requestDto.serialNumber = testSerialLink.getSerialNumber();
        // companyName is null

        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(requestDto)
        .when()
            .post("/customers/")
        .then()
            .statusCode(400)
            .body(containsString("Customer name cannot be empty"));

        // Assert: Verify nothing was persisted
        assertCustomerNotExists(testUser.getEmail());
    }

    @Test
    @DisplayName("POST /api/customers - Should return 400 when email is missing")
    void testCreateCustomer_MissingEmail() {
        // Arrange
        CustomerDTO requestDto = new CustomerDTO();
        requestDto.companyName = "Test Company";
        requestDto.serialNumber = testSerialLink.getSerialNumber();

        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(requestDto)
        .when()
            .post("/customers/")
        .then()
            .statusCode(400)
            .body(containsString("Customer email cannot be empty"));

        // Assert: Verify nothing was persisted
        long customerCount = countAllCustomers();
        assertEquals(0, customerCount);
    }

    @Test
    @DisplayName("POST /api/customers - Should return 400 when serial number is missing")
    void testCreateCustomer_MissingSerialNumber() {
        // Arrange
        CustomerDTO requestDto = new CustomerDTO();
        requestDto.email = testUser.getEmail();
        requestDto.companyName = "Test Company";

        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(requestDto)
        .when()
            .post("/customers/")
        .then()
            .statusCode(400)
            .body(containsString("Serial number cannot be empty"));

        // Assert: Verify nothing was persisted
        assertCustomerNotExists(testUser.getEmail());
    }

    @Test
    @DisplayName("POST /api/customers - Should return 403 when serial number is invalid")
    void testCreateCustomer_InvalidSerialNumber() {
        // Arrange
        CustomerDTO requestDto = new CustomerDTO();
        requestDto.email = testUser.getEmail();
        requestDto.companyName = "Test Company";
        requestDto.serialNumber = 99999999;

        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(requestDto)
        .when()
            .post("/customers/")
        .then()
            .statusCode(403)
            .body(containsString("Invalid serial number"));

        // Assert: Verify nothing was persisted
        assertCustomerNotExists(testUser.getEmail());
    }

    @Test
    @DisplayName("POST /api/customers - Should return 403 when email doesn't match serial number")
    void testCreateCustomer_EmailMismatch() {
        // Arrange
        CustomerDTO requestDto = new CustomerDTO();
        requestDto.email = "wrong@email.com";
        requestDto.companyName = "Test Company";
        requestDto.serialNumber = testSerialLink.getSerialNumber();

        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(requestDto)
        .when()
            .post("/customers/")
        .then()
            .statusCode(403)
            .body(containsString("Invalid serial number"));

        // Assert: Verify nothing was persisted
        assertCustomerNotExists("wrong@email.com");
    }

    @Test
    @DisplayName("POST /api/customers - Should return 400 when user doesn't exist")
    void testCreateCustomer_UserNotFound() {
        // Arrange
        String nonExistentEmail = "nonexistent@example.com";
        CustomerDTO requestDto = new CustomerDTO();
        requestDto.email = nonExistentEmail;
        requestDto.companyName = "Test Company";
        requestDto.serialNumber = 88888888;
        
        createSerialLinkFixture(88888888, "EXT-999", nonExistentEmail, testPlan.getName(), 100);

        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(requestDto)
        .when()
            .post("/customers/")
        .then()
            .statusCode(400)
            .body(containsString("No user found"));

        // Assert: Verify nothing was persisted
        assertCustomerNotExists(nonExistentEmail);
    }

    @Test
    @DisplayName("POST /api/customers - Should return 409 when customer already exists")
    void testCreateCustomer_CustomerAlreadyExists() {
        // Arrange: Create first customer
        CustomerDTO firstRequest = new CustomerDTO();
        firstRequest.email = testUser.getEmail();
        firstRequest.companyName = "First Company";
        firstRequest.serialNumber = testSerialLink.getSerialNumber();

        given()
            .contentType(ContentType.JSON)
            .body(firstRequest)
        .when()
            .post("/customers/")
        .then()
            .statusCode(201);

        // Act: Try to create duplicate customer
        CustomerDTO secondRequest = new CustomerDTO();
        secondRequest.email = testUser.getEmail();
        secondRequest.companyName = "Second Company";
        secondRequest.serialNumber = testSerialLink.getSerialNumber();

        given()
            .contentType(ContentType.JSON)
            .body(secondRequest)
        .when()
            .post("/customers/")
        .then()
            .statusCode(409)
            .body(containsString("Customer already exists"));

        // Assert: Verify only one customer exists
        long customerCount = countAllCustomers();
        assertEquals(1, customerCount);
        
        Customer customer = findCustomerByEmailOrFail(testUser.getEmail());
        assertEquals("First Company", customer.getCompanyName()); // Original name preserved
    }

    // ==================== Database Query Helpers ====================

    private Customer findCustomerByEmailOrFail(String email) {
        try (EntityManager em = emf.createEntityManager()) {
            Customer customer = em.createQuery(
                "SELECT c FROM Customer c WHERE c.user.email = :email", 
                Customer.class
            )
            .setParameter("email", email)
            .getSingleResult();
            
            return customer;
        } catch (NoResultException e) {
            fail("Expected customer with email " + email + " to exist, but not found");
            return null;
        }
    }

    private Subscription findSubscriptionByCustomerIdOrFail(Long customerId) {
        try (EntityManager em = emf.createEntityManager()) {
            Subscription subscription = em.createQuery(
                "SELECT s FROM Subscription s WHERE s.customer.id = :customerId", 
                Subscription.class
            )
            .setParameter("customerId", customerId)
            .getSingleResult();
            
            return subscription;
        } catch (NoResultException e) {
            fail("Expected subscription for customer ID " + customerId + " to exist, but not found");
            return null;
        }
    }

    private SmsBalance findSmsBalanceByExternalCustomerIdOrFail(String externalCustomerId) {
        try (EntityManager em = emf.createEntityManager()) {
            SmsBalance smsBalance = em.createQuery(
                "SELECT s FROM SmsBalance s WHERE s.externalCustomerId = :externalCustomerId", 
                SmsBalance.class
            )
            .setParameter("externalCustomerId", externalCustomerId)
            .getSingleResult();
            
            return smsBalance;
        } catch (NoResultException e) {
            fail("Expected SMS balance for external customer ID " + externalCustomerId + " to exist, but not found");
            return null;
        }
    }

    private void assertCustomerNotExists(String email) {
        try (EntityManager em = emf.createEntityManager()) {
            long count = em.createQuery(
                "SELECT COUNT(c) FROM Customer c WHERE c.user.email = :email", 
                Long.class
            )
            .setParameter("email", email)
            .getSingleResult();
            
            assertEquals(0, count, "Expected no customer with email " + email + " but found " + count);
        }
    }

    private long countAllCustomers() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT COUNT(c) FROM Customer c", Long.class)
                .getSingleResult();
        }
    }

    // ==================== Test Fixtures Setup ====================

    private void setupTestFixtures() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            Role userRole = new Role("USER");
            em.persist(userRole);

            testUser = new User("testuser@example.com", "password123");
            testUser.addRole(userRole);
            em.persist(testUser);

            testPlan = new Plan("Test Plan", Period.MONTHLY, 49900, Currency.DKK, "Test plan", true);
            em.persist(testPlan);

            testSerialLink = new SerialLink(
                12345678, 
                "EXT-TEST-001", 
                testUser.getEmail(), 
                testPlan.getName(), 
                500,
                java.time.OffsetDateTime.now().plusDays(30)
            );
            em.persist(testSerialLink);

            em.flush();
            em.getTransaction().commit();
        }
    }

    private void createSerialLinkFixture(Integer serialNumber, String externalCustomerId, 
                                        String expectedEmail, String planName, Integer initialSmsBalance) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            SerialLink serialLink = new SerialLink(
                serialNumber, 
                externalCustomerId, 
                expectedEmail, 
                planName, 
                initialSmsBalance,
                java.time.OffsetDateTime.now().plusDays(30)
            );
            em.persist(serialLink);
            em.flush();
            em.getTransaction().commit();
        }
    }

    private void cleanDatabase() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM SmsBalance").executeUpdate();
            em.createQuery("DELETE FROM Subscription").executeUpdate();
            em.createQuery("DELETE FROM Customer").executeUpdate();
            em.createQuery("DELETE FROM SerialLink").executeUpdate();
            em.createQuery("DELETE FROM Plan").executeUpdate();
            em.createQuery("DELETE FROM User").executeUpdate();
            em.createQuery("DELETE FROM Role").executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            // Ignore cleanup errors during test isolation
        }
    }
}

