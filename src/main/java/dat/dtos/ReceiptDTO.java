package dat.dtos;

import dat.enums.ReceiptStatus;

import java.time.OffsetDateTime;
import java.util.Map;

public class ReceiptDTO {
    public Long id;
    public Long paymentId;
    public String receiptNumber;
    public Integer priceCents;
    public OffsetDateTime paidAt;
    public ReceiptStatus status;
    public String processorReceiptUrl;
    public String customerEmail;
    public String companyName;
    public String pmBrand;
    public String pmLast4;
    public Integer pmExpYear;
    public String processorIntentId;
    public Map<String, Object> metadata;
    public OffsetDateTime createdAt;
}

