package dat.entities;

import dat.enums.Status;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class SerialLink {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = true)  // Changed: optional during pre-registration
    @JoinColumn(name = "customer_id", nullable = true)  // Changed: nullable before customer creation
    private Customer customer;
    
    @Column(unique = true)  // Added: serial numbers should be unique
    private Integer serialNumber;

    @ManyToOne(optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    private OffsetDateTime verifiedAt;
    private String externalProof;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

}
