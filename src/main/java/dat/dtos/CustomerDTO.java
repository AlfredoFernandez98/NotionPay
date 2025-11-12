package dat.dtos;

import java.time.OffsetDateTime;

public class CustomerDTO {
    public Long id;
    public String email;  // from User
    public String companyName;
    public Integer serialNumber;
    public String externalCustomerId;
    public OffsetDateTime createdAt;
}
