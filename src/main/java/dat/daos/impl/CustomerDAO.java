package dat.daos.impl;

import dat.daos.IDAO;
import dat.entities.Customer;
import dat.security.entities.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DAO for Customer entity
 * Implements IDAO interface and provides custom Customer operations
 * Follows Singleton pattern with getInstance()
 */
public class CustomerDAO implements IDAO<Customer> {
    private static CustomerDAO instance;
    private static EntityManagerFactory emf;

    /**
     * Get singleton instance of CustomerDAO
     * @param _emf EntityManagerFactory to use
     * @return CustomerDAO instance
     */
    public static CustomerDAO getInstance(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new CustomerDAO();
        }
        return instance;
    }

    private CustomerDAO() {
        // Private constructor for singleton
    }


    @Override
    public Customer create(Customer customer) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(customer);
            em.getTransaction().commit();
            return customer;
        }
    }

    @Override
    public Optional<Customer> getById(Long id) {
        try (EntityManager em = emf.createEntityManager()) {
            Customer customer = em.find(Customer.class, id);
            return Optional.ofNullable(customer);
        }
    }

    @Override
    public Set<Customer> getAll() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT c FROM Customer c", Customer.class)
                    .getResultList()
                    .stream()
                    .collect(Collectors.toSet());
        }
    }

    @Override
    public void update(Customer customer) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.merge(customer);
            em.getTransaction().commit();
        }
    }

    @Override
    public void delete(Long id) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Customer customer = em.find(Customer.class, id);
            if (customer != null) {
                em.remove(customer);
            }
            em.getTransaction().commit();
        }
    }

    @Override
    public Optional<Customer> findByName(String name) {
        try (EntityManager em = emf.createEntityManager()) {
            Customer customer = em.createQuery(
                "SELECT c FROM Customer c WHERE c.companyName = :name", 
                Customer.class
            )
            .setParameter("name", name)
            .getSingleResult();
            return Optional.of(customer);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    // ==================== Custom Customer Methods ====================

    /**
     * Create a Customer linked to a User with registration details
     * Used during user registration with SerialLink verification
     * @param user The User entity to link to
     * @param companyName The company name
     * @param serialNumber The serial number from registration
     * @return The created Customer entity
     */
    public Customer createCustomer(User user, String companyName, Integer serialNumber) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            
            Customer customer = new Customer();
            customer.setUser(user);
            customer.setCompanyName(companyName);
            customer.setSerialNumber(serialNumber);
            customer.setCreatedAt(OffsetDateTime.now());
            // externalCustomerId will be set later when Stripe integration is added
            
            em.persist(customer);
            em.getTransaction().commit();
            
            return customer;
        }
    }

    /**
     * Get Customer by User email
     * @param email The user's email
     * @return Optional containing the Customer, or empty if not found
     */
    public Optional<Customer> getByUserEmail(String email) {
        try (EntityManager em = emf.createEntityManager()) {
            Customer customer = em.createQuery(
                "SELECT c FROM Customer c WHERE c.user.email = :email", 
                Customer.class
            )
            .setParameter("email", email)
            .getSingleResult();
            return Optional.of(customer);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /**
     * Get Customer by serial number
     * @param serialNumber The serial number
     * @return Optional containing the Customer, or empty if not found
     */
    public Optional<Customer> getBySerialNumber(Integer serialNumber) {
        try (EntityManager em = emf.createEntityManager()) {
            Customer customer = em.createQuery(
                "SELECT c FROM Customer c WHERE c.serialNumber = :serialNumber", 
                Customer.class
            )
            .setParameter("serialNumber", serialNumber)
            .getSingleResult();
            return Optional.of(customer);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}
