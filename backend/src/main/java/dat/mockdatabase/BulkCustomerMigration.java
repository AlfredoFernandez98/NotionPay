package dat.mockdatabase;

import dat.config.HibernateConfig;
import dat.entities.Plan;
import dat.entities.Product;
import dat.entities.SerialLink;
import dat.enums.Currency;
import dat.enums.Period;
import dat.enums.ProductType;
import dat.enums.Status;
import dat.utils.DateTimeUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.time.OffsetDateTime;
import java.util.Random;

/**
 * Smart bulk migration for adding 100 mock customers
 * Uses intelligent checking to avoid duplicates
 * 
 * Strategy:
 * 1. Check if serial numbers already exist
 * 2. Only insert new ones
 * 3. Generate realistic test data
 * 
 * @author NotionPay Team
 */
public class BulkCustomerMigration {
    
    private static final Random random = new Random();
    
    // Company name prefixes for realistic data
    private static final String[] COMPANY_PREFIXES = {
        "Tech", "Digital", "Smart", "Global", "Nordic", "Euro", "Mega", "Super",
        "Quantum", "Cyber", "Cloud", "Data", "Micro", "Macro", "Ultra", "Prime"
    };
    
    private static final String[] COMPANY_SUFFIXES = {
        "Solutions", "Systems", "Technologies", "Innovations", "Dynamics", "Ventures",
        "Industries", "Enterprises", "Corporation", "Group", "Labs", "Works"
    };
    
    private static final String[] DOMAINS = {
        "tech.com", "digital.io", "solutions.net", "corp.dk", "company.com",
        "business.eu", "enterprise.com", "systems.io", "innovations.tech"
    };
    
    /**
     * Add 1000 mock customers intelligently
     * Only adds customers that don't already exist
     */
    public static void addBulkCustomers() {
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        EntityManager em = emf.createEntityManager();
        
        try {
            em.getTransaction().begin();
            
            // Check how many serial links already exist
            Long existingCount = em.createQuery("SELECT COUNT(s) FROM SerialLink s", Long.class)
                .getSingleResult();
            
            System.out.println("Current SerialLinks in database: " + existingCount);
            
            // Ensure Plans exist (needed for SerialLinks)
            ensurePlansExist(em);
            
            int addedCount = 0;
            int skippedCount = 0;
            
            // Generate 1000 customers starting from serial 200000000
            for (int i = 0; i < 1000; i++) {
                int serialNumber = 200000000 + i;
                
                // Check if this serial already exists
                Long count = em.createQuery(
                    "SELECT COUNT(s) FROM SerialLink s WHERE s.serialNumber = :serial", 
                    Long.class
                )
                .setParameter("serial", serialNumber)
                .getSingleResult();
                
                if (count > 0) {
                    skippedCount++;
                    continue; // Skip if already exists
                }
                
                // Generate realistic customer data
                String companyName = generateCompanyName();
                String email = generateEmail(companyName, i);
                String externalCustomerId = "cus_bulk_" + String.format("%03d", i);
                String planName = selectRandomPlan();
                int smsBalance = selectRandomSmsBalance();
                OffsetDateTime nextPaymentDate = generateRandomPaymentDate();
                
                // Create SerialLink
                SerialLink serialLink = new SerialLink(
                    serialNumber,
                    externalCustomerId,
                    email,
                    planName,
                    smsBalance,
                    nextPaymentDate
                );
                
                em.persist(serialLink);
                addedCount++;
                
                // Flush every 50 records to avoid memory issues
                if (addedCount % 50 == 0) {
                    em.flush();
                    System.out.println("Added " + addedCount + " customers...");
                }
            }
            
            em.getTransaction().commit();
            
            System.out.println("\nBulk customer migration completed!");
            System.out.println("   Added: " + addedCount + " new customers");
            System.out.println("   Skipped: " + skippedCount + " existing customers");
            System.out.println("   Total: " + (existingCount + addedCount) + " customers in database");
            
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("ERROR: Bulk migration failed: " + e.getMessage());
            throw new RuntimeException("Bulk migration failed", e);
        } finally {
            em.close();
        }
    }
    
    /**
     * Ensure Plans exist in database (needed for SerialLinks)
     */
    private static void ensurePlansExist(EntityManager em) {
        Long planCount = em.createQuery("SELECT COUNT(p) FROM Plan p", Long.class)
            .getSingleResult();
        
        if (planCount == 0) {
            System.out.println("Creating Plans...");
            createPlan(em, "Basic Monthly", Period.MONTHLY, 49900, "Basic features");
            createPlan(em, "Professional Monthly", Period.MONTHLY, 99900, "Professional features");
            createPlan(em, "Enterprise Yearly", Period.YEARLY, 999900, "Enterprise features");
            em.flush();
            System.out.println("Plans created");
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
    
    /**
     * Generate realistic company name
     */
    private static String generateCompanyName() {
        String prefix = COMPANY_PREFIXES[random.nextInt(COMPANY_PREFIXES.length)];
        String suffix = COMPANY_SUFFIXES[random.nextInt(COMPANY_SUFFIXES.length)];
        return prefix + " " + suffix;
    }
    
    /**
     * Generate email from company name
     */
    private static String generateEmail(String companyName, int index) {
        String domain = DOMAINS[random.nextInt(DOMAINS.length)];
        String localPart = companyName.toLowerCase()
            .replace(" ", "")
            .replaceAll("[^a-z0-9]", "");
        return localPart + index + "@" + domain;
    }
    
    /**
     * Select random plan (weighted distribution)
     */
    private static String selectRandomPlan() {
        int rand = random.nextInt(100);
        if (rand < 60) {
            return "Basic Monthly";  // 60% Basic
        } else if (rand < 90) {
            return "Professional Monthly";  // 30% Professional
        } else {
            return "Enterprise Yearly";  // 10% Enterprise
        }
    }
    
    /**
     * Select random SMS balance (realistic distribution)
     */
    private static int selectRandomSmsBalance() {
        int rand = random.nextInt(100);
        if (rand < 40) {
            return 100;  // 40% have 100 SMS
        } else if (rand < 70) {
            return 500;  // 30% have 500 SMS
        } else if (rand < 90) {
            return 1000;  // 20% have 1000 SMS
        } else {
            return random.nextInt(5000) + 1000;  // 10% have 1000-6000 SMS
        }
    }
    
    /**
     * Generate random payment date (within next 60 days)
     */
    private static OffsetDateTime generateRandomPaymentDate() {
        OffsetDateTime now = DateTimeUtil.now();
        int daysInFuture = random.nextInt(60);  // 0-60 days
        return now.plusDays(daysInFuture);
    }
    
    /**
     * Remove all bulk customers (cleanup method)
     * Only removes customers with serial numbers 200000000-200000999
     */
    public static void removeBulkCustomers() {
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        EntityManager em = emf.createEntityManager();
        
        try {
            em.getTransaction().begin();
            
            int deleted = em.createQuery(
                "DELETE FROM SerialLink s WHERE s.serialNumber >= 200000000 AND s.serialNumber < 200001000"
            ).executeUpdate();
            
            em.getTransaction().commit();
            
            System.out.println("Removed " + deleted + " bulk customers");
            
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Cleanup failed", e);
        } finally {
            em.close();
        }
    }
    
    /**
     * Get statistics about bulk customers
     */
    public static void printStatistics() {
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        EntityManager em = emf.createEntityManager();
        
        try {
            Long totalSerialLinks = em.createQuery("SELECT COUNT(s) FROM SerialLink s", Long.class)
                .getSingleResult();
            
            Long bulkCustomers = em.createQuery(
                "SELECT COUNT(s) FROM SerialLink s WHERE s.serialNumber >= 200000000 AND s.serialNumber < 200001000",
                Long.class
            ).getSingleResult();
            
            Long originalCustomers = totalSerialLinks - bulkCustomers;
            
            System.out.println("\nDatabase Statistics:");
            System.out.println("   Total SerialLinks: " + totalSerialLinks);
            System.out.println("   Original customers: " + originalCustomers);
            System.out.println("   Bulk customers: " + bulkCustomers);
            
        } catch (Exception e) {
            System.err.println("Failed to get statistics: " + e.getMessage());
        } finally {
            em.close();
        }
    }
}

