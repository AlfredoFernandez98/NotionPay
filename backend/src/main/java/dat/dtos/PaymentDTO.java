package dat.dtos;

import dat.enums.Currency;
import dat.enums.PaymentStatus;

import java.time.OffsetDateTime;

public class PaymentDTO {
    public Long id;
    public Long customerId;
    public Long paymentMethodId;
    public Long subscriptionId;
    public Long productId;
    public PaymentStatus status;
    public Integer priceCents;
    public Currency currency;
    public String processorIntentId;
    public OffsetDateTime createdAt;
}

