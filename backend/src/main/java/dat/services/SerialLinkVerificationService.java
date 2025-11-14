package dat.services;

import dat.entities.Customer;
import dat.entities.Plan;
import dat.entities.SerialLink;
import dat.enums.Status;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.time.OffsetDateTime;

/**
 * Service to verify serial numbers using SerialLink entity
 * More sophisticated than PreRegistrationData - includes Plan eligibility
 * Follows Singleton pattern with getInstance()
 */
public class SerialLinkVerificationService {
    private static SerialLinkVerificationService instance;
    private static EntityManagerFactory emf;

    /**
     * Get singleton instance of SerialLinkVerificationService
     * @param _emf EntityManagerFactory to use
     * @param email The email to verify
     * @return SerialLinkVerificationService instance
     */
    public static SerialLinkVerificationService getInstance(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new SerialLinkVerificationService();
        }
        return instance;
    }

    private SerialLinkVerificationService() {
        // Private constructor for singleton
    }

    /**
     * Verify if serialNumber exists in SerialLink with Pending status
     * @param serialNumber The serial number to verify
     * @return true if valid and pending, false otherwise
     */
    public boolean verifySerialNumberAndEmail(Integer serialNumber, String email) {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<SerialLink> query = em.createQuery(
                    "SELECT s FROM SerialLink s WHERE s.serialNumber = :serialNumber " +
                            "AND s.expectedEmail = :email AND s.status = :status",
                    SerialLink.class
            );
            query.setParameter("serialNumber", serialNumber);
            query. setParameter("email", email);
            query.setParameter("status", Status.PENDING);
            
            SerialLink serialLink = query.getSingleResult();
            
            // Valid if: Pending status AND no customer linked yet
            return serialLink.getCustomer() == null;
            
        } catch (NoResultException e) {
            return false;
        }
    }

    /**
     * Link a Customer to SerialLink and mark as Verified
     * @param serialNumber The serial number
     * @param customer The customer to link
     */
    public void linkCustomerToSerialLink(Integer serialNumber, Customer customer) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            
            TypedQuery<SerialLink> query = em.createQuery(
                "SELECT s FROM SerialLink s WHERE s.serialNumber = :serialNumber",
                SerialLink.class
            );
            query.setParameter("serialNumber", serialNumber);
            
            SerialLink serialLink = query.getSingleResult();
            
            // Link customer and update status
            serialLink.setCustomer(customer);
            serialLink.setStatus(Status.VERIFIED);
            serialLink.setVerifiedAt(OffsetDateTime.now());
            serialLink.setExternalProof("Verified via NotionPay Registration");
            serialLink.setUpdatedAt(OffsetDateTime.now());
            
            em.merge(serialLink);
            em.getTransaction().commit();
        }
    }

    /**
     * Get the Plan associated with a serial number
     * @param serialNumber The serial number
     * @return The Plan entity, or null if not found
     */
    public Plan getPlanForSerialNumber(Integer serialNumber) {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<Plan> query = em.createQuery(
                "SELECT s.plan FROM SerialLink s WHERE s.serialNumber = :serialNumber",
                Plan.class
            );
            query.setParameter("serialNumber", serialNumber);
            
            return query.getSingleResult();
            
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Get SerialLink by serial number
     * @param serialNumber The serial number
     * @return The SerialLink entity, or null if not found
     */
    public SerialLink getSerialLink(Integer serialNumber) {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<SerialLink> query = em.createQuery(
                "SELECT s FROM SerialLink s WHERE s.serialNumber = :serialNumber",
                SerialLink.class
            );
            query.setParameter("serialNumber", serialNumber);
            
            return query.getSingleResult();
            
        } catch (NoResultException e) {
            return null;
        }
    }
}
