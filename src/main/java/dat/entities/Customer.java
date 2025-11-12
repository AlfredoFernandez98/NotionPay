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
    @JoinColumn(name= "user_email", nullable = false)
    private User user;
    private String companyName;
    private int serialNumber;
    private OffsetDateTime createdAt;
    private String externalCustomerId;


}
