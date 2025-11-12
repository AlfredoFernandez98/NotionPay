package dat.mockdatabase;

import dat.config.HibernateConfig;
import dat.entities.Plan;
import dat.entities.SerialLink;
import dat.enums.Currency;
import dat.enums.Period;
import dat.enums.Status;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.time.OffsetDateTime;

/**
 * Mock Database Migration for SerialLink
 * Purpose: Simulates serial numbers from external database with Plan eligibility
 * This is more sophisticated than PreRegistrationData
 */
public class SerialLinkMigration {

    /**
     * Populate SerialLinks with associated Plans
     */
    public static void populateSerialLinksAndPlans() {
        System.out.println("\n🔗 Populating SerialLinks with Plans (Simulating External DB)");
        System.out.println("-".repeat(60));

        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();
            
            // ========== CREATE PLANS FIRST ==========
            
            // Plan 1: Basic Monthly Plan
            Plan basicPlan = new Plan();
            basicPlan.setName("Basic Monthly");
            basicPlan.setPeriod(Period.MONTHLY);
            basicPlan.setPriceCents(49900); // 499 DKK
            basicPlan.setCurrency(Currency.DKK);
            basicPlan.setDescription("Basic features with monthly billing");
            basicPlan.setActive(true);
            em.persist(basicPlan);
            
            // Plan 2: Professional Monthly Plan
            Plan proPlan = new Plan();
            proPlan.setName("Professional Monthly");
            proPlan.setPeriod(Period.MONTHLY);
            proPlan.setPriceCents(99900); // 999 DKK
            proPlan.setCurrency(Currency.DKK);
            proPlan.setDescription("Professional features with monthly billing");
            proPlan.setActive(true);
            em.persist(proPlan);
            
            // Plan 3: Enterprise Yearly Plan
            Plan enterprisePlan = new Plan();
            enterprisePlan.setName("Enterprise Yearly");
            enterprisePlan.setPeriod(Period.YEARLY);
            enterprisePlan.setPriceCents(999900); // 9999 DKK
            enterprisePlan.setCurrency(Currency.DKK);
            enterprisePlan.setDescription("Enterprise features with yearly billing");
            enterprisePlan.setActive(true);
            em.persist(enterprisePlan);
            
            System.out.println("✅ Created 3 Plans:");
            System.out.println("   - Basic Monthly (499 DKK/month)");
            System.out.println("   - Professional Monthly (999 DKK/month)");
            System.out.println("   - Enterprise Yearly (9999 DKK/year)");
            
            // ========== CREATE SERIAL LINKS ==========
            
            // SerialLink 1: Ellab A/S - Basic Plan - AVAILABLE
            SerialLink serial1 = new SerialLink();
            serial1.setSerialNumber(101010101);
            serial1.setPlan(basicPlan);
            serial1.setStatus(Status.PENDING);
            serial1.setCustomer(null); // No customer yet
            serial1.setExternalProof("Pre-registered via External System");
            serial1.setCreatedAt(OffsetDateTime.now());
            em.persist(serial1);
            
            // SerialLink 2: Notion Technologies - Professional Plan - AVAILABLE
            SerialLink serial2 = new SerialLink();
            serial2.setSerialNumber(404040404);
            serial2.setPlan(proPlan);
            serial2.setStatus(Status.PENDING);
            serial2.setCustomer(null);
            serial2.setExternalProof("Pre-registered via External System");
            serial2.setCreatedAt(OffsetDateTime.now());
            em.persist(serial2);
            
            // SerialLink 3: Startup Denmark - Enterprise Plan - AVAILABLE
            SerialLink serial3 = new SerialLink();
            serial3.setSerialNumber(505050505);
            serial3.setPlan(enterprisePlan);
            serial3.setStatus(Status.PENDING);
            serial3.setCustomer(null);
            serial3.setExternalProof("Pre-registered via External System");
            serial3.setCreatedAt(OffsetDateTime.now());
            em.persist(serial3);
            
            // SerialLink 4: BBB ApS - Basic Plan - ALREADY VERIFIED (for testing)
            SerialLink serial4 = new SerialLink();
            serial4.setSerialNumber(202020202);
            serial4.setPlan(basicPlan);
            serial4.setStatus(Status.VERIFIED);
            serial4.setCustomer(null); // Would be linked to a customer in real scenario
            serial4.setVerifiedAt(OffsetDateTime.now().minusDays(7));
            serial4.setExternalProof("Already verified in external system");
            serial4.setCreatedAt(OffsetDateTime.now().minusDays(30));
            em.persist(serial4);
            
            // SerialLink 5: Rejected Serial (for testing)
            SerialLink serial5 = new SerialLink();
            serial5.setSerialNumber(999999999);
            serial5.setPlan(basicPlan);
            serial5.setStatus(Status.REJECTED);
            serial5.setCustomer(null);
            serial5.setExternalProof("Rejected due to invalid external verification");
            serial5.setCreatedAt(OffsetDateTime.now().minusDays(15));
            em.persist(serial5);
            
            em.getTransaction().commit();
            
            // Print summary
            System.out.println("\n✅ Created 5 SerialLink records:");
            System.out.println("\n📋 AVAILABLE FOR REGISTRATION (Status: PENDING):");
            System.out.println("   1. Serial: 101010101 → Basic Monthly Plan");
            System.out.println("   2. Serial: 404040404 → Professional Monthly Plan");
            System.out.println("   3. Serial: 505050505 → Enterprise Yearly Plan");
            
            System.out.println("\n🚫 NOT AVAILABLE:");
            System.out.println("   1. Serial: 202020202 [VERIFIED - already used]");
            System.out.println("   2. Serial: 999999999 [REJECTED - invalid]");
            
            System.out.println("\n✅ SerialLink migration completed successfully");
            System.out.println("💡 Each serial number is now linked to a specific Plan!");
            
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("\n❌ SerialLink migration failed: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Migration failed", e);
        } finally {
            em.close();
        }
    }
    
    /**
     * Clear all SerialLinks and Plans (for testing/reset)
     */
    public static void clearAll() {
        System.out.println("\n🗑️  Clearing all SerialLinks and Plans...");
        
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        EntityManager em = emf.createEntityManager();
        
        try {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM SerialLink").executeUpdate();
            em.createQuery("DELETE FROM Plan").executeUpdate();
            em.getTransaction().commit();
            System.out.println("✅ All SerialLinks and Plans cleared");
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("❌ Failed to clear data: " + e.getMessage());
            throw new RuntimeException("Clear failed", e);
        } finally {
            em.close();
        }
    }
}

