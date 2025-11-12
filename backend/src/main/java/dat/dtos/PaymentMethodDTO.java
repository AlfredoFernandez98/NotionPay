package dat.dtos;

import dat.enums.PaymentMethodStatus;

import java.time.OffsetDateTime;

public class PaymentMethodDTO {
    public Long id;
    public Long customerId;
    public String type;
    public String brand;
    public String last4;
    public Integer expMonth;
    public Integer expYear;
    public String processorMethodId;
    public Boolean isDefault;
    public PaymentMethodStatus status;
    public String fingerprint;
    public OffsetDateTime createdAt;
    public OffsetDateTime updatedAt;
}

