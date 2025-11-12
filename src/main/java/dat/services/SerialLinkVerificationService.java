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
 */
public class SerialLinkVerificationService {
    private EntityManagerFactory emf;

    public SerialLinkVerificationService(EntityManagerFactory emf) {
        this.emf = emf;
    }

    /**
     * Verify if serialNumber exists in SerialLink with Pending status
     * @param serialNumber The serial number to verify
     * @return true if valid and pending, false otherwise
     */
    public boolean verifySerialNumber(Integer serialNumber) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<SerialLink> query = em.createQuery(
                "SELECT s FROM SerialLink s WHERE s.serialNumber = :serialNumber AND s.status = :status",
                SerialLink.class
            );
            query.setParameter("serialNumber", serialNumber);
            query.setParameter("status", Status.PENDING);
            
            SerialLink serialLink = query.getSingleResult();
            
            // Valid if: Pending status AND no customer linked yet
            return serialLink.getCustomer() == null;
            
        } catch (NoResultException e) {
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Link a Customer to SerialLink and mark as Verified
     * @param serialNumber The serial number
     * @param customer The customer to link
     */
    public void linkCustomerToSerialLink(Integer serialNumber, Customer customer) {
        EntityManager em = emf.createEntityManager();
        try {
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
            
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Get the Plan associated with a serial number
     * @param serialNumber The serial number
     * @return The Plan entity, or null if not found
     */
    public Plan getPlanForSerialNumber(Integer serialNumber) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Plan> query = em.createQuery(
                "SELECT s.plan FROM SerialLink s WHERE s.serialNumber = :serialNumber",
                Plan.class
            );
            query.setParameter("serialNumber", serialNumber);
            
            return query.getSingleResult();
            
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    /**
     * Get SerialLink by serial number
     * @param serialNumber The serial number
     * @return The SerialLink entity, or null if not found
     */
    public SerialLink getSerialLink(Integer serialNumber) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<SerialLink> query = em.createQuery(
                "SELECT s FROM SerialLink s WHERE s.serialNumber = :serialNumber",
                SerialLink.class
            );
            query.setParameter("serialNumber", serialNumber);
            
            return query.getSingleResult();
            
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }
}

