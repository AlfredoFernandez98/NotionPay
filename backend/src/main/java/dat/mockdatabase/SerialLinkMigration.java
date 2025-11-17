package dat.mockdatabase;

import dat.config.HibernateConfig;
import dat.entities.Plan;
import dat.entities.SerialLink;
import dat.entities.SmsBalance;
import dat.enums.Currency;
import dat.enums.Period;
import dat.enums.Status;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.time.OffsetDateTime;

/**
 * Mock Database Migration for SerialLink
 * 
 * Purpose: Simulates serial numbers from external database (e.g., SMS provider)
 * Each SerialLink represents a pre-registered company that can sign up
 * 
 * Key Data: serialNumber + expectedEmail + Plan
 * Registration flow: User enters email + serialNumber → System validates both match
 */
public class SerialLinkMigration {

    /**
     * Populate SerialLinks with Plans and Expected Emails
     * This simulates an external database of pre-registered companies
     */
    public static void populateSerialLinksAndPlans() {
        System.out.println("\n═══════════════════════════════════════════════════════════");
        System.out.println("  🔧 Populating SerialLinks (Simulating External Database)");
        System.out.println("═══════════════════════════════════════════════════════════");

        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();
            
            // ===== STEP 1: CREATE PLANS =====
            System.out.println("\n📋 Creating Plans...");
            
            Plan basicPlan = createPlan(em, "Basic Monthly", Period.MONTHLY, 49900, "Basic features");
            Plan proPlan = createPlan(em, "Professional Monthly", Period.MONTHLY, 99900, "Professional features");
            Plan enterprisePlan = createPlan(em, "Enterprise Yearly", Period.YEARLY, 999900, "Enterprise features");
            
            System.out.println("✅ Plans created:");
            System.out.println("   • Basic Monthly (499 DKK/month)");
            System.out.println("   • Professional Monthly (999 DKK/month)");
            System.out.println("   • Enterprise Yearly (9999 DKK/year)");
            
            // ===== STEP 2: CREATE SERIAL LINKS (Pre-registered companies) =====
            System.out.println("\n📋 Creating SerialLinks (Pre-registered Companies)...");
            
            // Example 1: Company A - can register with this email + serial
            createSerialLink(em, basicPlan, 101010101, "alice@company-a.com", "cus_ext_a_001");
            
            // Example 2: Company B - can register with this email + serial
            createSerialLink(em, proPlan, 404040404, "bob@company-b.com", "cus_ext_b_002");
            
            // Example 3: Company C - can register with this email + serial
            createSerialLink(em, enterprisePlan, 505050505, "charlie@company-c.com", "cus_ext_c_003");
            
            // Example 4: Already verified (used for testing edge cases)
            SerialLink verifiedSerial = createSerialLink(em, basicPlan, 202020202, "diana@company-d.com", "cus_ext_d_004");
            verifiedSerial.setStatus(Status.VERIFIED);
            verifiedSerial.setVerifiedAt(OffsetDateTime.now().minusDays(7));
            em.merge(verifiedSerial);
            
            // Example 5: Rejected serial (for testing)
            SerialLink rejectedSerial = createSerialLink(em, basicPlan, 999999999, "eve@company-e.com", "cus_ext_e_005");
            rejectedSerial.setStatus(Status.REJECTED);
            em.merge(rejectedSerial);
            
            em.getTransaction().commit();
            
            // ===== PRINT SUMMARY =====
            System.out.println("\n✅ SerialLinks Created Successfully!");
            
            System.out.println("\n📌 AVAILABLE FOR REGISTRATION (Status: PENDING):");
            System.out.println("   Serial 101010101 + alice@company-a.com → Basic Monthly");
            System.out.println("   Serial 404040404 + bob@company-b.com → Professional Monthly");
            System.out.println("   Serial 505050505 + charlie@company-c.com → Enterprise Yearly");
            
            System.out.println("\n⚠️  NOT AVAILABLE:");
            System.out.println("   Serial 202020202 [VERIFIED - already registered]");
            System.out.println("   Serial 999999999 [REJECTED - invalid]");
            
            System.out.println("\n💡 IMPORTANT: Email + SerialNumber must BOTH match to register!");
            System.out.println("   Example: alice@company-a.com + 101010101 ✅");
            System.out.println("   Example: bob@company-a.com + 101010101 ❌ (wrong email)");
            System.out.println("═══════════════════════════════════════════════════════════\n");
            
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
     * Helper method to create a Plan
     */
    private static Plan createPlan(EntityManager em, String name, Period period, 
                                    int priceCents, String description) {
        Plan plan = new Plan();
        plan.setName(name);
        plan.setPeriod(period);
        plan.setPriceCents(priceCents);
        plan.setCurrency(Currency.DKK);
        plan.setDescription(description);
        plan.setActive(true);
        em.persist(plan);
        return plan;
    }
    
    /**
     * Helper method to create a SerialLink
     * 
     * @param em EntityManager for persistence
     * @param plan The plan this serial is eligible for
     * @param serialNumber The serial number
     * @param expectedEmail The email that must match during registration
     * @param externalCustomerId The ID from external system
     */
    private static SerialLink createSerialLink(EntityManager em, Plan plan, int serialNumber,
                                                String expectedEmail, String externalCustomerId) {
        SerialLink serial = new SerialLink();
        serial.setSerialNumber(serialNumber);
        serial.setExpectedEmail(expectedEmail);  // ← Must match during registration!
        serial.setPlan(plan);
        serial.setStatus(Status.PENDING);
        serial.setCustomer(null);
        serial.setExternalCustomerId(externalCustomerId);
        serial.setExternalProof("Pre-registered via External System");
        serial.setCreatedAt(OffsetDateTime.now());
        em.persist(serial);
        return serial;
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
            em.createQuery("DELETE FROM SmsBalance").executeUpdate();
            em.createQuery("DELETE FROM SerialLink").executeUpdate();
            em.createQuery("DELETE FROM Plan").executeUpdate();
            em.getTransaction().commit();
            System.out.println("✅ All SerialLinks, Plans, and SMS Balances cleared");
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

