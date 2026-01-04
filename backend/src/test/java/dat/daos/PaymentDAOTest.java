package dat.daos;

import dat.config.HibernateConfig;
import dat.daos.impl.CustomerDAO;
import dat.daos.impl.PaymentDAO;
import dat.daos.impl.PaymentMethodDAO;
import dat.daos.impl.PlanDAO;
import dat.entities.*;
import dat.enums.*;
import dat.security.entities.Role;
import dat.security.entities.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for PaymentDAO
 * Tests core CRUD operations and payment-specific queries
 */
class PaymentDAOTest {

    private static EntityManagerFactory emf;
    private static PaymentDAO paymentDAO;
    private static PaymentMethodDAO paymentMethodDAO;
    private static CustomerDAO customerDAO;
    private static PlanDAO planDAO;
    
    // Test data
    private User testUser;
    private Customer testCustomer;
    private Plan testPlan;
    private PaymentMethod testPaymentMethod;
    private Payment testPayment;

    @BeforeAll
    static void setUpAll() {
        // Initialize test database with Testcontainers
        HibernateConfig.setTest(true);
        emf = HibernateConfig.getEntityManagerFactoryForTest();
        paymentDAO = PaymentDAO.getInstance(emf);
        paymentMethodDAO = PaymentMethodDAO.getInstance(emf);
        customerDAO = CustomerDAO.getInstance(emf);
        planDAO = PlanDAO.getInstance(emf);
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
     * Verifies that a payment can be created and persisted to the database
     */
    @Test
    @DisplayName("Create payment - should persist and return payment with generated ID")
    void testCreate() {
        // Arrange
        Payment payment = new Payment(
            testCustomer,
            testPaymentMethod,
            null, // no subscription
            null, // no product
            PaymentStatus.PENDING,
            5000, // 50.00 DKK
            Currency.DKK,
            "pi_test_12345"
        );

        // Act
        Payment createdPayment = paymentDAO.create(payment);

        // Assert
        assertNotNull(createdPayment);
        assertNotNull(createdPayment.getId(), "Payment ID should be generated");
        assertEquals(5000, createdPayment.getPriceCents());
        assertEquals(Currency.DKK, createdPayment.getCurrency());
        assertEquals(PaymentStatus.PENDING, createdPayment.getStatus());
        assertEquals("pi_test_12345", createdPayment.getProcessorIntentId());
        assertEquals(testCustomer.getId(), createdPayment.getCustomer().getId());
        assertEquals(testPaymentMethod.getId(), createdPayment.getPaymentMethod().getId());
        assertNotNull(createdPayment.getCreatedAt());
        
        // Verify it's persisted in the database
        Optional<Payment> fetchedPayment = paymentDAO.getById(createdPayment.getId());
        assertTrue(fetchedPayment.isPresent(), "Payment should be retrievable from database");
        assertEquals(createdPayment.getId(), fetchedPayment.get().getId());
    }

    /**
     * Test the getById() method
     * Verifies that a payment can be retrieved by ID
     */
    @Test
    @DisplayName("Get payment by ID - should return payment when exists")
    void testGetById_Found() {
        // Arrange
        Payment createdPayment = paymentDAO.create(testPayment);

        // Act
        Optional<Payment> result = paymentDAO.getById(createdPayment.getId());

        // Assert
        assertTrue(result.isPresent(), "Payment should be found");
        assertEquals(createdPayment.getId(), result.get().getId());
        assertEquals(testPayment.getPriceCents(), result.get().getPriceCents());
        assertEquals(testPayment.getCurrency(), result.get().getCurrency());
    }

    /**
     * Test getById() when payment doesn't exist
     * Verifies that an empty Optional is returned for non-existent ID
     */
    @Test
    @DisplayName("Get payment by ID - should return empty when not found")
    void testGetById_NotFound() {
        // Act
        Optional<Payment> result = paymentDAO.getById(99999L);

        // Assert
        assertFalse(result.isPresent(), "Should return empty Optional for non-existent ID");
    }

    /**
     * Test the getByCustomerId() method
     * Verifies that all payments for a customer can be retrieved
     */
    @Test
    @DisplayName("Get payments by customer ID - should return all customer payments")
    void testGetByCustomerId() {
        // Arrange - Create multiple payments for the same customer
        Payment payment1 = paymentDAO.create(testPayment);
        
        Payment payment2 = new Payment(
            testCustomer,
            testPaymentMethod,
            null,
            null,
            PaymentStatus.COMPLETED,
            10000,
            Currency.DKK,
            "pi_test_67890"
        );
        paymentDAO.create(payment2);

        // Act
        Set<Payment> payments = paymentDAO.getByCustomerId(testCustomer.getId());

        // Assert
        assertNotNull(payments);
        assertEquals(2, payments.size(), "Should return all payments for customer");
        assertTrue(payments.stream().anyMatch(p -> p.getId().equals(payment1.getId())));
        assertTrue(payments.stream().anyMatch(p -> p.getId().equals(payment2.getId())));
    }

    /**
     * Test getByCustomerId() when no payments exist
     * Verifies that an empty set is returned
     */
    @Test
    @DisplayName("Get payments by customer ID - should return empty set when no payments")
    void testGetByCustomerId_Empty() {
        // Act
        Set<Payment> payments = paymentDAO.getByCustomerId(testCustomer.getId());

        // Assert
        assertNotNull(payments);
        assertTrue(payments.isEmpty(), "Should return empty set when no payments exist");
    }

    /**
     * Test the getByStatus() method
     * Verifies that payments can be filtered by status
     */
    @Test
    @DisplayName("Get payments by status - should return only matching payments")
    void testGetByStatus() {
        // Arrange
        Payment completedPayment = new Payment(
            testCustomer,
            testPaymentMethod,
            null,
            null,
            PaymentStatus.COMPLETED,
            5000,
            Currency.DKK,
            "pi_completed_1"
        );
        paymentDAO.create(completedPayment);

        Payment pendingPayment = new Payment(
            testCustomer,
            testPaymentMethod,
            null,
            null,
            PaymentStatus.PENDING,
            3000,
            Currency.DKK,
            "pi_pending_1"
        );
        paymentDAO.create(pendingPayment);

        Payment failedPayment = new Payment(
            testCustomer,
            testPaymentMethod,
            null,
            null,
            PaymentStatus.FAILED,
            2000,
            Currency.DKK,
            "pi_failed_1"
        );
        paymentDAO.create(failedPayment);

        // Act
        Set<Payment> completedPayments = paymentDAO.getByStatus(PaymentStatus.COMPLETED);
        Set<Payment> pendingPayments = paymentDAO.getByStatus(PaymentStatus.PENDING);

        // Assert
        assertEquals(1, completedPayments.size(), "Should find 1 completed payment");
        assertEquals(1, pendingPayments.size(), "Should find 1 pending payment");
        assertEquals(PaymentStatus.COMPLETED, completedPayments.iterator().next().getStatus());
        assertEquals(PaymentStatus.PENDING, pendingPayments.iterator().next().getStatus());
    }

    /**
     * Test the getByProcessorIntentId() method
     * Verifies that a payment can be found by Stripe PaymentIntent ID
     */
    @Test
    @DisplayName("Get payment by Stripe intent ID - should find payment")
    void testGetByProcessorIntentId() {
        // Arrange
        String intentId = "pi_stripe_unique_12345";
        testPayment.setProcessorIntentId(intentId);
        paymentDAO.create(testPayment);

        // Act
        Optional<Payment> result = paymentDAO.getByProcessorIntentId(intentId);

        // Assert
        assertTrue(result.isPresent(), "Payment should be found by processor intent ID");
        assertEquals(intentId, result.get().getProcessorIntentId());
        assertEquals(testPayment.getPriceCents(), result.get().getPriceCents());
    }

    /**
     * Test getByProcessorIntentId() when not found
     * Verifies that empty Optional is returned
     */
    @Test
    @DisplayName("Get payment by Stripe intent ID - should return empty when not found")
    void testGetByProcessorIntentId_NotFound() {
        // Act
        Optional<Payment> result = paymentDAO.getByProcessorIntentId("pi_nonexistent");

        // Assert
        assertFalse(result.isPresent(), "Should return empty Optional for non-existent intent ID");
    }

    /**
     * Test the update() method
     * Verifies that a payment status can be updated
     */
    @Test
    @DisplayName("Update payment - should update payment status")
    void testUpdate() {
        // Arrange
        Payment payment = paymentDAO.create(testPayment);
        assertEquals(PaymentStatus.PENDING, payment.getStatus());

        // Act - Update status to COMPLETED
        payment.setStatus(PaymentStatus.COMPLETED);
        paymentDAO.update(payment);

        // Assert
        Optional<Payment> updated = paymentDAO.getById(payment.getId());
        assertTrue(updated.isPresent());
        assertEquals(PaymentStatus.COMPLETED, updated.get().getStatus());
    }

    /**
     * Test the getAll() method
     * Verifies that all payments can be retrieved
     */
    @Test
    @DisplayName("Get all payments - should return all persisted payments")
    void testGetAll() {
        // Arrange - Create multiple payments
        paymentDAO.create(testPayment);
        
        Payment payment2 = new Payment(
            testCustomer,
            testPaymentMethod,
            null,
            null,
            PaymentStatus.COMPLETED,
            7500,
            Currency.DKK,
            "pi_test_second"
        );
        paymentDAO.create(payment2);

        // Act
        Set<Payment> allPayments = paymentDAO.getAll();

        // Assert
        assertNotNull(allPayments);
        assertEquals(2, allPayments.size(), "Should return all payments");
    }

    /**
     * Test getAll() when no payments exist
     * Verifies that an empty set is returned
     */
    @Test
    @DisplayName("Get all payments - should return empty set when no payments exist")
    void testGetAll_Empty() {
        // Act
        Set<Payment> allPayments = paymentDAO.getAll();

        // Assert
        assertNotNull(allPayments);
        assertTrue(allPayments.isEmpty(), "Should return empty set when no payments exist");
    }

    /**
     * Test delete() method
     * Verifies that a payment can be deleted
     */
    @Test
    @DisplayName("Delete payment - should remove payment from database")
    void testDelete() {
        // Arrange
        Payment payment = paymentDAO.create(testPayment);
        Long paymentId = payment.getId();
        assertTrue(paymentDAO.getById(paymentId).isPresent());

        // Act
        paymentDAO.delete(paymentId);

        // Assert
        Optional<Payment> deleted = paymentDAO.getById(paymentId);
        assertFalse(deleted.isPresent(), "Payment should be deleted from database");
    }

    // ==================== Helper Methods ====================

    /**
     * Set up test data before each test
     */
    private void setupTestData() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            // Create Role
            Role userRole = new Role("USER");
            em.persist(userRole);

            // Create User
            testUser = new User("test@example.com", "password123");
            testUser.addRole(userRole);
            em.persist(testUser);

            // Create Customer
            testCustomer = new Customer(
                testUser,
                "Test Payment Company",
                11111,
                "EXT-PAY-001",
                OffsetDateTime.now()
            );
            em.persist(testCustomer);

            // Create Plan
            testPlan = new Plan(
                "Test Plan",
                Period.MONTHLY,
                9900,
                Currency.DKK,
                "Test plan for payments",
                true
            );
            em.persist(testPlan);

            // Create PaymentMethod
            testPaymentMethod = new dat.entities.PaymentMethod(
                testCustomer,
                "card",
                "visa",
                "4242",
                12,
                2025,
                "pm_test_123",
                true,
                PaymentMethodStatus.ACTIVE,
                "fingerprint_123"
            );
            em.persist(testPaymentMethod);

            // Create test Payment (not persisted yet, will be used in tests)
            testPayment = new Payment(
                testCustomer,
                testPaymentMethod,
                null, // no subscription
                null, // no product
                PaymentStatus.PENDING,
                9900,
                Currency.DKK,
                "pi_test_intent_123"
            );

            em.getTransaction().commit();
        }
    }

    /**
     * Clean database by removing all test data
     */
    private void cleanDatabase() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            
            // Delete in order to respect foreign key constraints
            em.createQuery("DELETE FROM Receipt").executeUpdate();
            em.createQuery("DELETE FROM Payment").executeUpdate();
            em.createQuery("DELETE FROM PaymentMethod").executeUpdate();
            em.createQuery("DELETE FROM SmsBalance").executeUpdate();
            em.createQuery("DELETE FROM Subscription").executeUpdate();
            em.createQuery("DELETE FROM Customer").executeUpdate();
            em.createQuery("DELETE FROM Plan").executeUpdate();
            em.createQuery("DELETE FROM SerialLink").executeUpdate();
            em.createQuery("DELETE FROM User").executeUpdate();
            em.createQuery("DELETE FROM Role").executeUpdate();
            
            em.getTransaction().commit();
        } catch (Exception e) {
            // Ignore errors during cleanup
        }
    }
}

