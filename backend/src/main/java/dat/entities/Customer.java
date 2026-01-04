package dat.entities;

import dat.security.entities.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Customer {
    @Id
    @GeneratedValue
    @Column(name = "customer_id", nullable = false)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_email", nullable = false, unique = true)
    private User user;
    
    @Column(name = "company_name", unique = true)
    private String companyName;
    
    @Column(name = "serial_number", unique = true)
    private Integer serialNumber;
    
    @Column(name = "external_customer_id", unique = true)
    private String externalCustomerId;
    
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    public Customer(User user, String companyName, Integer serialNumber, String externalCustomerId, OffsetDateTime createdAt) {
        this.user = user;
        this.companyName = companyName;
        this.serialNumber = serialNumber;
        this.externalCustomerId = externalCustomerId;
        this.createdAt = createdAt;
    }
}
