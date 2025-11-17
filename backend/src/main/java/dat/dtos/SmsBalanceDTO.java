package dat.dtos;

import java.time.OffsetDateTime;

public class SmsBalanceDTO {
    public Long id;
    public String externalCustomerId;  // Link to Customer via external_customer_id
    public Integer totalSmsCredits;
    public Integer usedSmsCredits;
    public Integer remainingSmsCredits;
    public OffsetDateTime lastRechargedAt;
}

