package dat.dtos;

import dat.enums.AnchorPolicy;
import dat.enums.SubscriptionStatus;

import java.time.OffsetDateTime;

public class SubscriptionDTO {
    public Long id;
    public Long customerId;
    public String customerEmail;
    public Long planId;
    public String planName;
    public SubscriptionStatus status;
    public OffsetDateTime startDate;
    public OffsetDateTime endDate;
    public OffsetDateTime nextBillingDate;
    public AnchorPolicy anchorPolicy;
}

