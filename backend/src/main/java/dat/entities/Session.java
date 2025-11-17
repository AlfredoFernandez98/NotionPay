package dat.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Session {
    @Id
    @GeneratedValue
    @Column(name = "session_id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    @Column(name = "last_seen_at")
    private OffsetDateTime lastSeenAt;

    private String ip;

    @Column(name = "customer_agent")
    private String customerAgent;

    private Boolean active;

    public Session(Customer customer, OffsetDateTime expiresAt, String ip, String customerAgent) {
        this.customer = customer;
        this.createdAt = OffsetDateTime.now();
        this.expiresAt = expiresAt;
        this.lastSeenAt = OffsetDateTime.now();
        this.ip = ip;
        this.customerAgent = customerAgent;
        this.active = true;
    }
}

