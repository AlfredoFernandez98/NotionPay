package dat.security.daos;


import dat.security.dtos.UserDTO;
import dat.security.entities.Role;
import dat.security.entities.User;
import dat.security.exceptions.ApiException;
import dat.security.exceptions.ValidationException;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;

import java.util.stream.Collectors;


/**
 * Purpose: To handle security in the API
 * Author: Thomas Hartmann
 */
public class SecurityDAO implements ISecurityDAO {

    private static ISecurityDAO instance;
    private static EntityManagerFactory emf;

    public SecurityDAO(EntityManagerFactory _emf) {
        emf = _emf;
    }

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    @Override
    public UserDTO getVerifiedUser(String email, String password) throws ValidationException {
        try (EntityManager em = getEntityManager()) {
            User user = em.find(User.class, email);
            if (user == null)
                throw new EntityNotFoundException("No user found with email: " + email); //RuntimeException
            user.getRoles().size(); // force roles to be fetched from db
            if (!user.verifyPassword(password))
                throw new ValidationException("Wrong password");
            return new UserDTO(user.getEmail(), user.getRoles().stream().map(r -> r.getRoleName()).collect(Collectors.toSet()));
        }
    }

    @Override
    public User createUser(String email, String password) {
        try (EntityManager em = getEntityManager()) {
            User userEntity = em.find(User.class, email);
            if (userEntity != null)
                throw new EntityExistsException("User with email: " + email + " already exists");
            userEntity = new User(email, password);
            em.getTransaction().begin();
            Role userRole = em.find(Role.class, "user");
            if (userRole == null)
                userRole = new Role("user");
            em.persist(userRole);
            userEntity.addRole(userRole);
            em.persist(userEntity);
            em.getTransaction().commit();
            return userEntity;
        }catch (Exception e){
            e.printStackTrace();
            throw new ApiException(400, e.getMessage());
        }
    }

    @Override
    public User addRole(UserDTO userDTO, String newRole) {
        try (EntityManager em = getEntityManager()) {
            User user = em.find(User.class, userDTO.getEmail());
            if (user == null)
                throw new EntityNotFoundException("No user found with email: " + userDTO.getEmail());
            em.getTransaction().begin();
                Role role = em.find(Role.class, newRole);
                if (role == null) {
                    role = new Role(newRole);
                    em.persist(role);
                }
                user.addRole(role);
                //em.merge(user);
            em.getTransaction().commit();
            return user;
        }
    }
}

