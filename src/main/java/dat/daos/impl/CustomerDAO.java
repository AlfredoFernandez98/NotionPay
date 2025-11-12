package dat.daos.impl;

import dat.daos.IDAO;
import dat.dtos.CustomerDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.Set;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class CustomerDAO implements IDAO<CustomerDTO> {

    private static CustomerDAO instance;
    private static EntityManagerFactory emf;

    public static CustomerDAO getInstance(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new CustomerDAO();
        }
        return instance;
    }


    @Override
    public CustomerDTO create(CustomerDTO entity) {
        try(EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            // look if the email is already in or in use or is the email and serial number valid and match with each other.



            // Implementation for creating a Customer entity goes here
            em.getTransaction().commit();
            return entity; // Return the created entity (should be replaced with actual entity)
        } catch (Exception e) {
            throw new RuntimeException("Error creating Customer: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<CustomerDTO> getById(Long id) {
        try(EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            // Implementation for retrieving a Customer entity by ID goes here
            em.getTransaction().commit();
            return Optional.empty(); // Replace with actual retrieval logic
        }
    }

    @Override
    public Set<CustomerDTO> getAll() {
        return Set.of();
    }

    @Override
    public void update(CustomerDTO entity) {

    }

    @Override
    public void delete(Long id) {

    }

    @Override
    public Optional<CustomerDTO> findByName(String name) {
        return Optional.empty();
    }
}
