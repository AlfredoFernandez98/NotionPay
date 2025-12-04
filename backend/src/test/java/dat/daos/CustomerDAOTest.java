package dat.daos;

import dat.config.HibernateConfig;
import dat.daos.impl.CustomerDAO;
import dat.entities.Customer;
import dat.entities.SerialLink;
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
 * Unit test for CustomerDAO
 * Tests core CRUD operations used in the current application flow
 */
class CustomerDAOTest {

    private static EntityManagerFactory emf;
    private static CustomerDAO customerDAO;
    
    // Test data
    private User testUser;
    private SerialLink testSerialLink;
    private Customer testCustomer;

    @BeforeAll
    static void setUpAll() {
        // Initialize test database with Testcontainers
        HibernateConfig.setTest(true);
        emf = HibernateConfig.getEntityManagerFactoryForTest();
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
     * Verifies that a customer can be created and persisted to the database
     */
    @Test
    @DisplayName("Create customer - should persist and return customer with generated ID")
    void testCreate() {
        // Arrange
        Customer customer = new Customer(
            testUser,
            "Test Company",
            12345,
            "EXT-123",
            OffsetDateTime.now()
        );

        // Act
        Customer createdCustomer = customerDAO.create(customer);

        // Assert
        assertNotNull(createdCustomer);
        assertNotNull(createdCustomer.getId(), "Customer ID should be generated");
        assertEquals("Test Company", createdCustomer.getCompanyName());
        assertEquals(12345, createdCustomer.getSerialNumber());
        assertEquals("EXT-123", createdCustomer.getExternalCustomerId());
        assertEquals(testUser.getEmail(), createdCustomer.getUser().getEmail());
        
        // Verify it's persisted in the database
        Optional<Customer> fetchedCustomer = customerDAO.getById(createdCustomer.getId());
        assertTrue(fetchedCustomer.isPresent(), "Customer should be retrievable from database");
        assertEquals(createdCustomer.getId(), fetchedCustomer.get().getId());
    }

    /**
     * Test the getByUserEmail() method
     * Verifies that a customer can be retrieved by their user's email
     */
    @Test
    @DisplayName("Get customer by user email - should return customer when exists")
    void testGetByUserEmail_Found() {
        // Arrange
        customerDAO.create(testCustomer);

        // Act
        Optional<Customer> result = customerDAO.getByUserEmail(testUser.getEmail());

        // Assert
        assertTrue(result.isPresent(), "Customer should be found");
        assertEquals(testCustomer.getCompanyName(), result.get().getCompanyName());
        assertEquals(testUser.getEmail(), result.get().getUser().getEmail());
    }

    /**
     * Test getByUserEmail() when customer doesn't exist
     * Verifies that an empty Optional is returned for non-existent email
     */
    @Test
    @DisplayName("Get customer by user email - should return empty when not found")
    void testGetByUserEmail_NotFound() {
        // Act
        Optional<Customer> result = customerDAO.getByUserEmail("nonexistent@example.com");

        // Assert
        assertFalse(result.isPresent(), "Should return empty Optional for non-existent email");
    }

    /**
     * Test the createCustomer() method
     * Verifies that a customer can be created during registration with SerialLink data
     */
    @Test
    @DisplayName("Create customer with serial number - should fetch external customer ID from SerialLink")
    void testCreateCustomer() {
        // Arrange
        String companyName = "New Company";
        Integer serialNumber = testSerialLink.getSerialNumber();

        // Act
        Customer createdCustomer = customerDAO.createCustomer(testUser, companyName, serialNumber);

        // Assert
        assertNotNull(createdCustomer);
        assertNotNull(createdCustomer.getId(), "Customer ID should be generated");
        assertEquals(companyName, createdCustomer.getCompanyName());
        assertEquals(serialNumber, createdCustomer.getSerialNumber());
        assertEquals(testSerialLink.getExternalCustomerId(), createdCustomer.getExternalCustomerId(),
            "External customer ID should be fetched from SerialLink");
        assertEquals(testUser.getEmail(), createdCustomer.getUser().getEmail());
        assertNotNull(createdCustomer.getCreatedAt());
        
        // Verify it's persisted in the database
        Optional<Customer> fetchedCustomer = customerDAO.getByUserEmail(testUser.getEmail());
        assertTrue(fetchedCustomer.isPresent(), "Customer should be retrievable from database");
        assertEquals(createdCustomer.getExternalCustomerId(), fetchedCustomer.get().getExternalCustomerId());
    }

    /**
     * Test createCustomer() with non-existent serial number
     * Verifies proper exception handling when SerialLink doesn't exist
     */
    @Test
    @DisplayName("Create customer with invalid serial number - should throw exception")
    void testCreateCustomer_InvalidSerialNumber() {
        // Arrange
        String companyName = "Invalid Company";
        Integer invalidSerialNumber = 99999;

        // Act & Assert
        assertThrows(Exception.class, () -> {
            customerDAO.createCustomer(testUser, companyName, invalidSerialNumber);
        }, "Should throw exception when SerialLink doesn't exist");
    }

    /**
     * Test the getAll() method
     * Verifies that all customers can be retrieved
     */
    @Test
    @DisplayName("Get all customers - should return all persisted customers")
    void testGetAll() {
        // Arrange - Create multiple customers
        Customer customer1 = customerDAO.create(testCustomer);
        
        User user2 = createAndPersistUser("test2@example.com", "password");
        Customer customer2 = new Customer(
            user2,
            "Company Two",
            54321,
            "EXT-456",
            OffsetDateTime.now()
        );
        customerDAO.create(customer2);

        // Act
        Set<Customer> allCustomers = customerDAO.getAll();

        // Assert
        assertNotNull(allCustomers);
        assertEquals(2, allCustomers.size(), "Should return all customers");
        
        // Verify both customers are in the result
        assertTrue(allCustomers.stream()
            .anyMatch(c -> c.getCompanyName().equals("Test Company Ltd")));
        assertTrue(allCustomers.stream()
            .anyMatch(c -> c.getCompanyName().equals("Company Two")));
    }

    /**
     * Test getAll() when no customers exist
     * Verifies that an empty set is returned
     */
    @Test
    @DisplayName("Get all customers - should return empty set when no customers exist")
    void testGetAll_Empty() {
        // Act
        Set<Customer> allCustomers = customerDAO.getAll();

        // Assert
        assertNotNull(allCustomers);
        assertTrue(allCustomers.isEmpty(), "Should return empty set when no customers exist");
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

            // Create SerialLink
            testSerialLink = new SerialLink(
                11111,
                "EXT-CUST-001",
                "test@example.com",
                "BASIC",
                100,
                java.time.OffsetDateTime.now().plusDays(30)
            );
            em.persist(testSerialLink);

            // Create test Customer (not persisted yet, will be used in tests)
            testCustomer = new Customer(
                testUser,
                "Test Company Ltd",
                11111,
                "EXT-CUST-001",
                OffsetDateTime.now()
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
            em.createQuery("DELETE FROM Customer").executeUpdate();
            em.createQuery("DELETE FROM SerialLink").executeUpdate();
            em.createQuery("DELETE FROM User").executeUpdate();
            em.createQuery("DELETE FROM Role").executeUpdate();
            
            em.getTransaction().commit();
        } catch (Exception e) {
            // Ignore errors during cleanup
        }
    }

    /**
     * Helper method to create and persist a User with Role
     */
    private User createAndPersistUser(String email, String password) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            // Find or create role
            Role userRole = em.find(Role.class, "USER");
            if (userRole == null) {
                userRole = new Role("USER");
                em.persist(userRole);
            }

            User user = new User(email, password);
            user.addRole(userRole);
            em.persist(user);

            em.getTransaction().commit();
            return user;
        }
    }
}

