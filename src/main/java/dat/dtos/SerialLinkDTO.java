package dat.dtos;

import dat.enums.Status;

import java.time.OffsetDateTime;

public class SerialLinkDTO {
    public Long id;
    public Long customerId;
    public Integer serialNumber;
    public Long planId;
    public String planName;
    public Status status;
    public OffsetDateTime verifiedAt;
    public String externalProof;
    public OffsetDateTime createdAt;
    public OffsetDateTime updatedAt;
}
