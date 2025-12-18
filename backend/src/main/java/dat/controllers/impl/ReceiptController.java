package dat.controllers.impl;

import dat.controllers.IController;
import dat.daos.impl.ReceiptDAO;
import dat.dtos.ReceiptDTO;
import dat.entities.Receipt;
import io.javalin.http.Context;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller for Receipt endpoints
 * Handles receipt retrieval and filtering
 */
public class ReceiptController implements IController<ReceiptDTO> {
    private static final Logger logger = LoggerFactory.getLogger(ReceiptController.class);
    
    private final ReceiptDAO receiptDAO;

    public ReceiptController(EntityManagerFactory emf) {
        this.receiptDAO = ReceiptDAO.getInstance(emf);
    }

    /**
     * GET /api/receipts/{id}
     * Get receipt by ID
     */
    @Override
    public void read(Context ctx) {
        try {
            Long id = Long.parseLong(ctx.pathParam("id"));
            Optional<Receipt> receipt = receiptDAO.getById(id);
            
            if (receipt.isEmpty()) {
                ctx.status(404).json("{\"msg\": \"Receipt not found with ID: " + id + "\"}");
                return;
            }
            
            ReceiptDTO dto = convertToDTO(receipt.get());
            ctx.status(200).json(dto);
            logger.info("Retrieved receipt ID: {}", id);
            
        } catch (NumberFormatException e) {
            ctx.status(400).json("{\"msg\": \"Invalid receipt ID format\"}");
        } catch (Exception e) {
            logger.error("Error retrieving receipt: ", e);
            ctx.status(500).json("{\"msg\": \"Failed to retrieve receipt: " + e.getMessage() + "\"}");
        }
    }

    /**
     * GET /api/customers/{customerId}/receipts
     * Get all receipts for a customer
     */
    public void getCustomerReceipts(Context ctx) {
        try {
            Long customerId = Long.parseLong(ctx.pathParam("customerId"));
            Set<Receipt> receipts = receiptDAO.getByCustomerId(customerId);
            
            // Convert to DTOs
            List<ReceiptDTO> dtos = receipts.stream()
                    .map(this::convertToDTO)
                    .sorted((a, b) -> b.createdAt.compareTo(a.createdAt)) // Sort by date, newest first
                    .collect(Collectors.toList());
            
            ctx.status(200).json(dtos);
            logger.info("Retrieved {} receipts for customer ID: {}", dtos.size(), customerId);
            
        } catch (NumberFormatException e) {
            ctx.status(400).json("{\"msg\": \"Invalid customer ID format\"}");
        } catch (Exception e) {
            logger.error("Error retrieving customer receipts: ", e);
            ctx.status(500).json("{\"msg\": \"Failed to retrieve receipts: " + e.getMessage() + "\"}");
        }
    }

    /**
     * GET /api/receipts/number/{receiptNumber}
     * Get receipt by receipt number
     */
    public void getByReceiptNumber(Context ctx) {
        try {
            String receiptNumber = ctx.pathParam("receiptNumber");
            Optional<Receipt> receipt = receiptDAO.getByReceiptNumber(receiptNumber);
            
            if (receipt.isEmpty()) {
                ctx.status(404).json("{\"msg\": \"Receipt not found with number: " + receiptNumber + "\"}");
                return;
            }
            
            ReceiptDTO dto = convertToDTO(receipt.get());
            ctx.status(200).json(dto);
            logger.info("Retrieved receipt by number: {}", receiptNumber);
            
        } catch (Exception e) {
            logger.error("Error retrieving receipt by number: ", e);
            ctx.status(500).json("{\"msg\": \"Failed to retrieve receipt: " + e.getMessage() + "\"}");
        }
    }

    // ==================== Helper Methods ====================

    private ReceiptDTO convertToDTO(Receipt receipt) {
        ReceiptDTO dto = new ReceiptDTO();
        dto.id = receipt.getId();
        dto.paymentId = receipt.getPayment().getId();
        dto.receiptNumber = receipt.getReceiptNumber();
        dto.priceCents = receipt.getPriceCents();
        dto.paidAt = receipt.getPaidAt();
        dto.status = receipt.getStatus();
        dto.processorReceiptUrl = receipt.getProcessorReceiptUrl();
        dto.customerEmail = receipt.getCustomerEmail();
        dto.companyName = receipt.getCompanyName();
        dto.pmBrand = receipt.getPmBrand();
        dto.pmLast4 = receipt.getPmLast4();
        dto.pmExpYear = receipt.getPmExpYear();
        dto.processorIntentId = receipt.getProcessorIntentId();
        dto.metadata = receipt.getMetadata();
        dto.createdAt = receipt.getCreatedAt();
        return dto;
    }

    // ==================== IController Interface ====================

    @Override
    public void readAll(Context ctx) {
        ctx.status(501).json("{\"msg\": \"Use customer-specific endpoint: GET /api/customers/{id}/receipts\"}");
    }

    @Override
    public void create(Context ctx) {
        ctx.status(501).json("{\"msg\": \"Receipts are created automatically with payments\"}");
    }

    @Override
    public void update(Context ctx) {
        ctx.status(501).json("{\"msg\": \"Receipts cannot be updated once created\"}");
    }

    @Override
    public void delete(Context ctx) {
        ctx.status(501).json("{\"msg\": \"Receipts cannot be deleted\"}");
    }
}
