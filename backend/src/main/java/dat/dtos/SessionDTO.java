package dat.dtos;

import java.time.OffsetDateTime;

public class SessionDTO {
    public Long id;
    public Long customerId;
    public String customerEmail;
    public OffsetDateTime createdAt;
    public OffsetDateTime expiresAt;
    public OffsetDateTime lastSeenAt;
    public String ip;
    public String customerAgent;
    public Boolean active;
}

