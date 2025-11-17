package dat.daos.impl;

import dat.daos.IDAO;
import dat.entities.Payment;
import jakarta.persistence.EntityManagerFactory;

import java.util.Optional;
import java.util.Set;

/**
 * DAO for Payment entity
 * TODO: Implement IDAO interface methods (create, getById, getAll, update, delete)
 * TODO: Add custom business methods like getByCustomerId, getByStatus, etc.
 */
public class PaymentDAO implements IDAO<Payment> {
    private static PaymentDAO instance;
    private static EntityManagerFactory emf;

    public static PaymentDAO getInstance(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new PaymentDAO();
        }
        return instance;
    }

    private PaymentDAO() {
        // Private constructor for singleton
    }

    @Override
    public Payment create(Payment payment) {
        // TODO: Implement create
        // Pattern: try-with-resources, begin transaction, persist, commit
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<Payment> getById(Long id) {
        // TODO: Implement getById
        // Pattern: try-with-resources, em.find()
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Set<Payment> getAll() {
        // TODO: Implement getAll
        // Pattern: TypedQuery with "SELECT p FROM Payment p"
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void update(Payment payment) {
        // TODO: Implement update
        // Pattern: try-with-resources, begin transaction, merge, commit
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void delete(Long id) {
        // TODO: Implement delete
        // Pattern: find entity first, then remove
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<Payment> findByName(String name) {
        return Optional.empty();
    }

    // ========== CUSTOM BUSINESS METHODS ==========

    /**
     * Get all payments for a specific customer
     * TODO: Implement this query
     */
    public Set<Payment> getByCustomerId(Long customerId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Get payments by status
     * TODO: Implement this query
     */
    public Set<Payment> getByStatus(String status) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}

