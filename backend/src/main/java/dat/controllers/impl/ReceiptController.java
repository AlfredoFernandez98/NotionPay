package dat.controllers.impl;

import dat.controllers.IController;
import dat.daos.impl.ReceiptDAO;
import dat.dtos.ReceiptDTO;
import dat.entities.Receipt;
import dat.utils.ErrorResponse;
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
                ErrorResponse.notFound(ctx, "Receipt not found with ID: " + id);
                return;
            }
            
            ReceiptDTO dto = convertToDTO(receipt.get());
            ctx.status(200).json(dto);
            logger.info("Retrieved receipt ID: {}", id);
            
        } catch (NumberFormatException e) {
            ErrorResponse.badRequest(ctx, "Invalid receipt ID format");
        } catch (Exception e) {
            ErrorResponse.internalError(ctx, "Failed to retrieve receipt", logger, e);
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
            ErrorResponse.badRequest(ctx, "Invalid customer ID format");
        } catch (Exception e) {
            ErrorResponse.internalError(ctx, "Failed to retrieve receipts", logger, e);
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
                ErrorResponse.notFound(ctx, "Receipt not found with number: " + receiptNumber);
                return;
            }
            
            ReceiptDTO dto = convertToDTO(receipt.get());
            ctx.status(200).json(dto);
            logger.info("Retrieved receipt by number: {}", receiptNumber);
            
        } catch (Exception e) {
            ErrorResponse.internalError(ctx, "Failed to retrieve receipt", logger, e);
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
        ErrorResponse.notImplemented(ctx, "Use customer-specific endpoint: GET /api/customers/{id}/receipts");
    }

    @Override
    public void create(Context ctx) {
        ErrorResponse.notImplemented(ctx, "Receipts are created automatically with payments");
    }

    @Override
    public void update(Context ctx) {
        ErrorResponse.notImplemented(ctx, "Receipts cannot be updated once created");
    }

    @Override
    public void delete(Context ctx) {
        ErrorResponse.notImplemented(ctx, "Receipts cannot be deleted");
    }
}
