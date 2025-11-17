package dat.daos.impl;

import dat.daos.IDAO;
import dat.entities.SmsBalance;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DAO for SmsBalance entity
 * Simulates access to external SMS provider database
 * Follows singleton pattern with getInstance()
 */
public class SmsBalanceDAO implements IDAO<SmsBalance> {
    private static SmsBalanceDAO instance;
    private static EntityManagerFactory emf;

    /**
     * Get singleton instance of SmsBalanceDAO
     */
    public static SmsBalanceDAO getInstance(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new SmsBalanceDAO();
        }
        return instance;
    }

    private SmsBalanceDAO() {
        // Private constructor for singleton
    }

    @Override
    public SmsBalance create(SmsBalance smsBalance) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(smsBalance);
            em.getTransaction().commit();
            return smsBalance;
        }
    }

    @Override
    public Optional<SmsBalance> getById(Long id) {
        try (EntityManager em = emf.createEntityManager()) {
            SmsBalance balance = em.find(SmsBalance.class, id);
            return Optional.ofNullable(balance);
        }
    }

    @Override
    public Set<SmsBalance> getAll() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT s FROM SmsBalance s", SmsBalance.class)
                    .getResultStream()
                    .collect(Collectors.toSet());
        }
    }

    @Override
    public void update(SmsBalance smsBalance) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.merge(smsBalance);
            em.getTransaction().commit();
        }
    }

    @Override
    public void delete(Long id) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            SmsBalance balance = em.find(SmsBalance.class, id);
            if (balance != null) {
                em.remove(balance);
            }
            em.getTransaction().commit();
        }
    }

    @Override
    public Optional<SmsBalance> findByName(String name) {
        return Optional.empty(); // SmsBalance doesn't have a name field
    }

    // ========== CUSTOM BUSINESS METHODS ==========

    /**
     * Get SMS Balance by external_customer_id
     */
    public Optional<SmsBalance> getByExternalCustomerId(String externalCustomerId) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                    "SELECT s FROM SmsBalance s WHERE s.externalCustomerId = :externalId",
                    SmsBalance.class)
                    .setParameter("externalId", externalCustomerId)
                    .getResultStream()
                    .findFirst();
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /**
     * Use SMS credits for a customer
     */
    public boolean useSmsCredits(String externalCustomerId, int count) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Optional<SmsBalance> balanceOpt = getByExternalCustomerId(externalCustomerId);
            
            if (balanceOpt.isPresent()) {
                SmsBalance balance = balanceOpt.get();
                boolean success = balance.useSms(count);
                if (success) {
                    em.merge(balance);
                    em.getTransaction().commit();
                    return true;
                }
            }
            em.getTransaction().rollback();
            return false;
        }
    }

    /**
     * Recharge SMS credits for a customer
     */
    public void rechargeSmsCredits(String externalCustomerId, int credits) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Optional<SmsBalance> balanceOpt = getByExternalCustomerId(externalCustomerId);
            
            if (balanceOpt.isPresent()) {
                SmsBalance balance = balanceOpt.get();
                balance.recharge(credits);
                em.merge(balance);
            }
            em.getTransaction().commit();
        }
    }
}
