package dat.entities;

import dat.enums.AnchorPolicy;
import dat.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Subscription {
    
    @Id
    @GeneratedValue
    @Column(name = "subscription_id")
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;
    
    @Column(name = "start_date", nullable = false)
    private OffsetDateTime startDate;
    
    @Column(name = "end_date")
    private OffsetDateTime endDate;
    
    @Column(name = "next_billing_date")
    private OffsetDateTime nextBillingDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "anchor_policy", nullable = false)
    private AnchorPolicy anchorPolicy;

    public Subscription(Customer customer, Plan plan, SubscriptionStatus status, 
                       OffsetDateTime startDate, OffsetDateTime nextBillingDate, AnchorPolicy anchorPolicy) {
        this.customer = customer;
        this.plan = plan;
        this.status = status;
        this.startDate = startDate;
        this.nextBillingDate = nextBillingDate;
        this.anchorPolicy = anchorPolicy;
    }
}
