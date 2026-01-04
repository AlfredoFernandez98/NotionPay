package dat.services;

import dat.entities.Plan;
import dat.entities.SerialLink;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;

/**
 * Service to verify serial numbers and fetch associated data
 */
public class SerialLinkVerificationService {
    private static SerialLinkVerificationService instance;
    private static EntityManagerFactory emf;

    public static SerialLinkVerificationService getInstance(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new SerialLinkVerificationService();
        }
        return instance;
    }

    private SerialLinkVerificationService() {}

    /**
     * Verify serial number + email combination
     */
    public boolean verifySerialNumberAndEmail(Integer serialNumber, String email) {
        try (EntityManager em = emf.createEntityManager()) {
            em.createQuery(
                "SELECT s FROM SerialLink s WHERE s.serialNumber = :serialNumber AND s.expectedEmail = :email",
                SerialLink.class
            )
            .setParameter("serialNumber", serialNumber)
            .setParameter("email", email)
            .getSingleResult();
            return true;
        } catch (NoResultException e) {
            return false;
        }
    }

    /**
     * Get Plan by serial number
     */
    public Plan getPlanForSerialNumber(Integer serialNumber) {
        try (EntityManager em = emf.createEntityManager()) {
            SerialLink serialLink = em.createQuery(
                "SELECT s FROM SerialLink s WHERE s.serialNumber = :serialNumber",
                SerialLink.class
            )
            .setParameter("serialNumber", serialNumber)
            .getSingleResult();
            
            // Get Plan by name
            return em.createQuery(
                "SELECT p FROM Plan p WHERE p.name = :name",
                Plan.class
            )
            .setParameter("name", serialLink.getPlanName())
            .getSingleResult();
            
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Get SerialLink by serial number
     */
    public SerialLink getSerialLink(Integer serialNumber) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                "SELECT s FROM SerialLink s WHERE s.serialNumber = :serialNumber",
                SerialLink.class
            )
            .setParameter("serialNumber", serialNumber)
            .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
