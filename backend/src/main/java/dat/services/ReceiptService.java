package dat.services;

import dat.daos.impl.ReceiptDAO;
import dat.entities.Receipt;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Set;

/**
 * Service for managing receipts
 * Handles receipt retrieval and filtering
 * 
 * @author NotionPay Team
 */
public class ReceiptService {
    private static ReceiptService instance;
    private static final Logger logger = LoggerFactory.getLogger(ReceiptService.class);
    
    private final ReceiptDAO receiptDAO;

    public static ReceiptService getInstance(EntityManagerFactory emf) {
        if (instance == null) {
            instance = new ReceiptService(emf);
        }
        return instance;
    }

    private ReceiptService(EntityManagerFactory emf) {
        this.receiptDAO = ReceiptDAO.getInstance(emf);
        logger.info("ReceiptService initialized");
    }

    /**
     * Get receipt by ID
     */
    public Optional<Receipt> getById(Long id) {
        return receiptDAO.getById(id);
    }

    /**
     * Get all receipts for a customer
     */
    public Set<Receipt> getByCustomerId(Long customerId) {
        return receiptDAO.getByCustomerId(customerId);
    }

    /**
     * Get receipt by receipt number
     */
    public Optional<Receipt> getByReceiptNumber(String receiptNumber) {
        return receiptDAO.getByReceiptNumber(receiptNumber);
    }

    /**
     * Get receipt by payment ID
     */
    public Optional<Receipt> getByPaymentId(Long paymentId) {
        return receiptDAO.getByPaymentId(paymentId);
    }
}
