package dat.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import jakarta.persistence.NoResultException;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for Payment flow: HTTP → Controller → DAO → Database
 * Tests complete payment processing with REAL Stripe integration
 * 
 * ⚠️ IMPORTANT: These tests make REAL calls to Stripe's test API
 * 
 * REQUIREMENTS:
 * 1. Valid Stripe test API keys in config.properties (✅ Already configured)
 * 2. Internet connection to api.stripe.com
 * 3. Testcontainers PostgreSQL (test_db) for isolated testing
 * 
 * HOW TO RUN THESE TESTS:
 * 
 * Run ONLY payment tests (recommended):
 *   mvn test -Dtest=PaymentIntegrationTest
 * 
 * Run ONLY unit tests (no Stripe needed):
 *   mvn test -Dtest=PaymentDAOTest
 * 
 * Run ONLY customer tests:
 *   mvn test -Dtest=CustomerIntegrationTest
 * 
 * ⚠️ DON'T run 'mvn test' (all tests) - it causes EntityManager conflicts
 * ⚠️ Run each test file individually for best results
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PaymentIntegrationTest {

    private static final int TEST_PORT = 7778;
    private static final String BASE_URL = "http://localhost:" + TEST_PORT;
    private static final String BASE_PATH = "/api";
    
    private EntityManagerFactory emf;
    private Javalin app;
    private ObjectMapper objectMapper = new ObjectMapper();
    
    // Test fixtures
    private User testUser;
    private Customer testCustomer;
    private Plan testPlan;
    private PaymentMethod testPaymentMethod;
    private String authToken;

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
        
        // Clear any cached entities from the EMF
        if (emf != null && emf.getCache() != null) {
            emf.getCache().evictAll();
        }
        
        setupTestFixtures();
        authenticateTestUser();
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test to ensure isolation
        cleanDatabase();
        
        // Clear any cached entities
        if (emf != null && emf.getCache() != null) {
            emf.getCache().evictAll();
        }
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
    @DisplayName("POST /api/payment-methods - Should save card and return 201")
    void testAddPaymentMethod_Success() {
        // Arrange
        // ⚠️ NOTE: In production, NEVER send raw card numbers to backend!
        // This is ONLY for testing. In production:
        // 1. Frontend uses Stripe.js to tokenize card
        // 2. Frontend sends token to backend (not card number)
        // 3. Backend uses token with Stripe API
        ObjectNode request = objectMapper.createObjectNode()
                .put("customerId", testCustomer.getId())
                .put("cardNumber", "4242424242424242")  // Test card - OK in test mode
                .put("expMonth", 12)
                .put("expYear", 2025)
                .put("cvc", "123")
                .put("isDefault", true);

        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request.toString())
        .when()
            .post("/payment-methods/")
        .then()
            .statusCode(201)
            .body("msg", containsString("Payment method added successfully"))
            .body("paymentMethodId", notNullValue())
            .body("brand", equalTo("visa"))
            .body("last4", equalTo("4242"));

        // Verify payment method persisted in database
        PaymentMethod savedMethod = findPaymentMethodByLast4OrFail("4242");
        assertNotNull(savedMethod.getId());
        assertEquals("visa", savedMethod.getBrand());
        assertEquals(testCustomer.getId(), savedMethod.getCustomer().getId());
        assertTrue(savedMethod.getIsDefault());
        assertEquals(PaymentMethodStatus.ACTIVE, savedMethod.getStatus());
    }

    @Test
    @DisplayName("POST /api/payment-methods - Should fail with invalid card number")
    void testAddPaymentMethod_InvalidCard() {
        // Arrange - Invalid card number
        ObjectNode request = objectMapper.createObjectNode()
                .put("customerId", testCustomer.getId())
                .put("cardNumber", "1234")
                .put("expMonth", 12)
                .put("expYear", 2025)
                .put("cvc", "123")
                .put("isDefault", true);

        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request.toString())
        .when()
            .post("/payment-methods/")
        .then()
            .statusCode(400)
            .body("msg", notNullValue());

        // Verify nothing was persisted
        assertEquals(1, countAllPaymentMethods()); // Only the fixture payment method exists
    }

    @Test
    @DisplayName("POST /api/payments - Should process payment and return 201")
    void testProcessPayment_Success() {
        // Arrange
        ObjectNode request = objectMapper.createObjectNode()
                .put("customerId", testCustomer.getId())
                .put("paymentMethodId", testPaymentMethod.getId())
                .put("amount", 9900)
                .put("currency", "dkk")
                .put("description", "Test subscription payment");

        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request.toString())
        .when()
            .post("/payments/")
        .then()
            .statusCode(201)
            .body("msg", containsString("Payment processed successfully"))
            .body("paymentId", notNullValue())
            .body("status", oneOf("COMPLETED", "PENDING"))
            .body("amount", equalTo(9900))
            .body("currency", equalTo("dkk"));

        // Verify payment persisted in database
        Payment savedPayment = findPaymentByAmountOrFail(9900);
        assertNotNull(savedPayment.getId());
        assertEquals(testCustomer.getId(), savedPayment.getCustomer().getId());
        assertEquals(testPaymentMethod.getId(), savedPayment.getPaymentMethod().getId());
        assertEquals(9900, savedPayment.getPriceCents());
        assertEquals(Currency.DKK, savedPayment.getCurrency());
        assertNotNull(savedPayment.getProcessorIntentId());
        assertNotNull(savedPayment.getCreatedAt());
    }

    @Test
    @DisplayName("POST /api/payments - Should create receipt for successful payment")
    void testProcessPayment_CreatesReceipt() {
        // Arrange
        ObjectNode request = objectMapper.createObjectNode()
                .put("customerId", testCustomer.getId())
                .put("paymentMethodId", testPaymentMethod.getId())
                .put("amount", 5000)
                .put("currency", "dkk")
                .put("description", "Receipt test payment");

        // Act
        String response = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request.toString())
        .when()
            .post("/payments/")
        .then()
            .statusCode(201)
            .body("receiptNumber", notNullValue())
            .extract().asString();

        // Extract payment ID from response
        Long paymentId = extractPaymentId(response);

        // Verify receipt was created
        Receipt receipt = findReceiptByPaymentIdOrFail(paymentId);
        assertNotNull(receipt.getId());
        assertEquals(paymentId, receipt.getPayment().getId());
        assertEquals(5000, receipt.getPriceCents());
        assertEquals(testUser.getEmail(), receipt.getCustomerEmail());
        assertEquals(testCustomer.getCompanyName(), receipt.getCompanyName());
        assertEquals("visa", receipt.getPmBrand());
        assertEquals("4242", receipt.getPmLast4());
        assertNotNull(receipt.getReceiptNumber());
        assertNotNull(receipt.getCreatedAt());
    }

    @Test
    @DisplayName("GET /api/payments/{id} - Should return payment details")
    void testGetPayment_Success() {
        // Arrange - Create a payment first
        Payment payment = createTestPaymentInDatabase(10000, PaymentStatus.COMPLETED);

        // Act & Assert
        given()
            .header("Authorization", "Bearer " + authToken)
        .when()
            .get("/payments/" + payment.getId())
        .then()
            .statusCode(200)
            .body("id", equalTo(payment.getId().intValue()))
            .body("customerId", equalTo(testCustomer.getId().intValue()))
            .body("paymentMethodId", equalTo(testPaymentMethod.getId().intValue()))
            .body("status", equalTo("COMPLETED"))
            .body("priceCents", equalTo(10000))
            .body("currency", equalTo("DKK"));
    }

    @Test
    @DisplayName("GET /api/payments/{id} - Should return 404 for non-existent payment")
    void testGetPayment_NotFound() {
        // Act & Assert
        given()
            .header("Authorization", "Bearer " + authToken)
        .when()
            .get("/payments/99999")
        .then()
            .statusCode(404)
            .body("msg", containsString("Payment not found"));
    }

    @Test
    @DisplayName("GET /api/customers/{id}/payments - Should return all customer payments")
    void testGetCustomerPayments_Success() {
        // Arrange - Create multiple payments
        createTestPaymentInDatabase(5000, PaymentStatus.COMPLETED);
        createTestPaymentInDatabase(7500, PaymentStatus.COMPLETED);
        createTestPaymentInDatabase(3000, PaymentStatus.PENDING);

        // Act & Assert
        given()
            .header("Authorization", "Bearer " + authToken)
        .when()
            .get("/customers/" + testCustomer.getId() + "/payments")
        .then()
            .statusCode(200)
            .body("size()", equalTo(3))
            .body("[0].customerId", equalTo(testCustomer.getId().intValue()))
            .body("[1].customerId", equalTo(testCustomer.getId().intValue()))
            .body("[2].customerId", equalTo(testCustomer.getId().intValue()));
    }

    @Test
    @DisplayName("GET /api/customers/{id}/payments - Should return empty array when no payments")
    void testGetCustomerPayments_Empty() {
        // Act & Assert
        given()
            .header("Authorization", "Bearer " + authToken)
        .when()
            .get("/customers/" + testCustomer.getId() + "/payments")
        .then()
            .statusCode(200)
            .body("size()", equalTo(0));
    }

    @Test
    @DisplayName("GET /api/payments/{id}/receipt - Should return receipt details")
    void testGetReceipt_Success() {
        // Arrange - Create payment with receipt
        Payment payment = createTestPaymentInDatabase(15000, PaymentStatus.COMPLETED);
        Receipt receipt = createTestReceiptInDatabase(payment);

        // Act & Assert
        given()
            .header("Authorization", "Bearer " + authToken)
        .when()
            .get("/payments/" + payment.getId() + "/receipt")
        .then()
            .statusCode(200)
            .body("id", equalTo(receipt.getId().intValue()))
            .body("receiptNumber", notNullValue())
            .body("priceCents", equalTo(15000))
            .body("customerEmail", equalTo(testUser.getEmail()))
            .body("companyName", equalTo(testCustomer.getCompanyName()));
    }

    @Test
    @DisplayName("GET /api/payments/{id}/receipt - Should return 404 when receipt not found")
    void testGetReceipt_NotFound() {
        // Arrange - Create payment without receipt
        Payment payment = createTestPaymentInDatabase(5000, PaymentStatus.PENDING);

        // Act & Assert
        given()
            .header("Authorization", "Bearer " + authToken)
        .when()
            .get("/payments/" + payment.getId() + "/receipt")
        .then()
            .statusCode(404)
            .body("msg", containsString("Receipt not found"));
    }

    @Test
    @DisplayName("POST /api/payments - Should return 404 for non-existent customer")
    void testProcessPayment_CustomerNotFound() {
        // Arrange
        ObjectNode request = objectMapper.createObjectNode()
                .put("customerId", 99999)
                .put("paymentMethodId", testPaymentMethod.getId())
                .put("amount", 5000)
                .put("currency", "dkk");

        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request.toString())
        .when()
            .post("/payments/")
        .then()
            .statusCode(404)
            .body("msg", containsString("Customer not found"));

        // Verify no payment was created
        assertEquals(0, countAllPayments());
    }

    @Test
    @DisplayName("POST /api/payments - Should return 404 for non-existent payment method")
    void testProcessPayment_PaymentMethodNotFound() {
        // Arrange
        ObjectNode request = objectMapper.createObjectNode()
                .put("customerId", testCustomer.getId())
                .put("paymentMethodId", 99999)
                .put("amount", 5000)
                .put("currency", "dkk");

        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request.toString())
        .when()
            .post("/payments/")
        .then()
            .statusCode(404)
            .body("msg", containsString("Payment method not found"));

        // Verify no payment was created
        assertEquals(0, countAllPayments());
    }

    @Test
    @DisplayName("POST /api/payments - Should return 401 without authentication")
    void testProcessPayment_Unauthorized() {
        // Arrange
        ObjectNode request = objectMapper.createObjectNode()
                .put("customerId", testCustomer.getId())
                .put("paymentMethodId", testPaymentMethod.getId())
                .put("amount", 5000)
                .put("currency", "dkk");

        // Act & Assert - No Authorization header
        given()
            .contentType(ContentType.JSON)
            .body(request.toString())
        .when()
            .post("/payments/")
        .then()
            .statusCode(401);

        // Verify no payment was created
        assertEquals(0, countAllPayments());
    }

    // ==================== Database Query Helpers ====================

    private PaymentMethod findPaymentMethodByLast4OrFail(String last4) {
        try (EntityManager em = emf.createEntityManager()) {
            PaymentMethod method = em.createQuery(
                "SELECT pm FROM PaymentMethod pm WHERE pm.last4 = :last4", 
                PaymentMethod.class
            )
            .setParameter("last4", last4)
            .getSingleResult();
            
            return method;
        } catch (NoResultException e) {
            fail("Expected payment method with last4 " + last4 + " to exist, but not found");
            return null;
        }
    }

    private Payment findPaymentByAmountOrFail(Integer amount) {
        try (EntityManager em = emf.createEntityManager()) {
            Payment payment = em.createQuery(
                "SELECT p FROM Payment p WHERE p.priceCents = :amount ORDER BY p.createdAt DESC", 
                Payment.class
            )
            .setParameter("amount", amount)
            .setMaxResults(1)
            .getSingleResult();
            
            return payment;
        } catch (NoResultException e) {
            fail("Expected payment with amount " + amount + " to exist, but not found");
            return null;
        }
    }

    private Receipt findReceiptByPaymentIdOrFail(Long paymentId) {
        try (EntityManager em = emf.createEntityManager()) {
            Receipt receipt = em.createQuery(
                "SELECT r FROM Receipt r WHERE r.payment.id = :paymentId", 
                Receipt.class
            )
            .setParameter("paymentId", paymentId)
            .getSingleResult();
            
            return receipt;
        } catch (NoResultException e) {
            fail("Expected receipt for payment ID " + paymentId + " to exist, but not found");
            return null;
        }
    }

    private long countAllPaymentMethods() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT COUNT(pm) FROM PaymentMethod pm", Long.class)
                .getSingleResult();
        }
    }

    private long countAllPayments() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT COUNT(p) FROM Payment p", Long.class)
                .getSingleResult();
        }
    }

    private Payment createTestPaymentInDatabase(Integer amount, PaymentStatus status) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            
            Payment payment = new Payment(
                testCustomer,
                testPaymentMethod,
                null,
                null,
                status,
                amount,
                Currency.DKK,
                "pi_test_" + System.currentTimeMillis()
            );
            em.persist(payment);
            
            em.getTransaction().commit();
            return payment;
        }
    }

    private Receipt createTestReceiptInDatabase(Payment payment) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            
            Receipt receipt = new Receipt(
                payment,
                "RCP-" + System.currentTimeMillis(),
                payment.getPriceCents(),
                java.time.OffsetDateTime.now(),
                ReceiptStatus.PAID,
                "https://stripe.com/receipt/test",
                testUser.getEmail(),
                testCustomer.getCompanyName(),
                "visa",
                "4242",
                2025,
                payment.getProcessorIntentId(),
                new java.util.HashMap<>()
            );
            em.persist(receipt);
            
            em.getTransaction().commit();
            return receipt;
        }
    }

    private Long extractPaymentId(String jsonResponse) {
        try {
            ObjectNode node = objectMapper.readValue(jsonResponse, ObjectNode.class);
            return node.get("paymentId").asLong();
        } catch (Exception e) {
            fail("Failed to extract paymentId from response");
            return null;
        }
    }

    // ==================== Test Fixtures Setup ====================

    private void setupTestFixtures() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            // Check if Role exists, if not create it
            Role userRole = em.find(Role.class, "USER");
            if (userRole == null) {
                userRole = new Role("USER");
                em.persist(userRole);
            }

            // Create User
            testUser = new User("payment-test@example.com", "password123");
            testUser.addRole(userRole);
            em.persist(testUser);

            // Create Customer
            testCustomer = new Customer(
                testUser,
                "Payment Test Company",
                12345678,
                "EXT-PAY-TEST-001",
                java.time.OffsetDateTime.now()
            );
            em.persist(testCustomer);

            // Create Plan
            testPlan = new Plan(
                "Test Plan",
                Period.MONTHLY,
                9900,
                Currency.DKK,
                "Test plan",
                true
            );
            em.persist(testPlan);

            // Create PaymentMethod (simulating already saved card)
            testPaymentMethod = new PaymentMethod(
                testCustomer,
                "card",
                "visa",
                "4242",
                12,
                2025,
                "pm_test_existing",
                true,
                PaymentMethodStatus.ACTIVE,
                "fingerprint_test"
            );
            em.persist(testPaymentMethod);

            em.getTransaction().commit();
        }
    }

    private void authenticateTestUser() {
        // Wait a bit for the database to be ready
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Login to get JWT token
        ObjectNode loginRequest = objectMapper.createObjectNode()
                .put("email", "payment-test@example.com")
                .put("password", "password123");

        String response = given()
            .contentType(ContentType.JSON)
            .body(loginRequest.toString())
        .when()
            .post("/auth/login")
        .then()
            .statusCode(200)
            .extract().asString();

        // Extract token
        try {
            ObjectNode node = objectMapper.readValue(response, ObjectNode.class);
            authToken = node.get("token").asText();
        } catch (Exception e) {
            fail("Failed to extract token from login response: " + e.getMessage());
        }
    }

    private void cleanDatabase() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            
            // Delete in correct order to respect foreign key constraints
            em.createQuery("DELETE FROM Receipt").executeUpdate();
            em.createQuery("DELETE FROM Payment").executeUpdate();
            em.createQuery("DELETE FROM Session").executeUpdate();
            em.createQuery("DELETE FROM ActivityLog").executeUpdate();
            em.createQuery("DELETE FROM PaymentMethod").executeUpdate();
            em.createQuery("DELETE FROM SmsBalance").executeUpdate();
            em.createQuery("DELETE FROM Subscription").executeUpdate();
            em.createQuery("DELETE FROM Customer").executeUpdate();
            em.createQuery("DELETE FROM Plan").executeUpdate();
            em.createQuery("DELETE FROM SerialLink").executeUpdate();
            em.createQuery("DELETE FROM User").executeUpdate();
            // Don't delete Role - it can be reused across tests
            
            em.getTransaction().commit();
        } catch (Exception e) {
            // Rollback on error
            try (EntityManager em2 = emf.createEntityManager()) {
                if (em2.getTransaction().isActive()) {
                    em2.getTransaction().rollback();
                }
            } catch (Exception ignored) {}
        }
    }
}

