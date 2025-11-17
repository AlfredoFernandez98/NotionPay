package dat.mockdatabase;

import dat.config.HibernateConfig;
import dat.entities.Plan;
import dat.entities.SerialLink;
import dat.enums.Currency;
import dat.enums.Period;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

/**
 * Database migration for test data
 * Populates Plans and SerialLinks (lookup table for serial_number → external_customer_id + email + plan)
 */
public class SerialLinkMigration {

    public static void populateSerialLinksAndPlans() {
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();
            
            createPlan(em, "Basic Monthly", Period.MONTHLY, 49900, "Basic features");
            createPlan(em, "Professional Monthly", Period.MONTHLY, 99900, "Professional features");
            createPlan(em, "Enterprise Yearly", Period.YEARLY, 999900, "Enterprise features");
            
            createSerialLink(em, 101010101, "cus_ext_a_001", "alice@company-a.com", "Basic Monthly", 100);
            createSerialLink(em, 404040404, "cus_ext_b_002", "bob@company-b.com", "Professional Monthly", 500);
            createSerialLink(em, 505050505, "cus_ext_c_003", "charlie@company-c.com", "Enterprise Yearly", 1000);
            createSerialLink(em, 202020202, "cus_ext_d_004", "diana@company-d.com", "Basic Monthly", 100);
            createSerialLink(em, 999999999, "cus_ext_e_005", "eve@company-e.com", "Basic Monthly", 100);
            
            em.getTransaction().commit();
            System.out.println("Created 3 Plans and 5 SerialLinks");
            
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("Migration failed: " + e.getMessage());
            throw new RuntimeException("Migration failed", e);
        } finally {
            em.close();
        }
    }
    
    private static void createPlan(EntityManager em, String name, Period period, int priceCents, String description) {
        Plan plan = new Plan();
        plan.setName(name);
        plan.setPeriod(period);
        plan.setPriceCents(priceCents);
        plan.setCurrency(Currency.DKK);
        plan.setDescription(description);
        plan.setActive(true);
        em.persist(plan);
    }
    
    private static void createSerialLink(EntityManager em, int serialNumber, String externalCustomerId, 
                                         String expectedEmail, String planName, int initialSmsBalance) {
        SerialLink serial = new SerialLink(serialNumber, externalCustomerId, expectedEmail, planName, initialSmsBalance);
        em.persist(serial);
    }
    
    public static void clearAll() {
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        EntityManager em = emf.createEntityManager();
        
        try {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Subscription").executeUpdate();
            em.createQuery("DELETE FROM Customer").executeUpdate();
            em.createQuery("DELETE FROM SmsBalance").executeUpdate();
            em.createQuery("DELETE FROM SerialLink").executeUpdate();
            em.createQuery("DELETE FROM Plan").executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Clear failed", e);
        } finally {
            em.close();
        }
    }
}

