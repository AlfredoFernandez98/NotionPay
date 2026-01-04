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
@Table(name = "sms_balance")
public class SmsBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_customer_id", nullable = false, unique = true)
    private String externalCustomerId;  // Links to Customer.external_customer_id

    @Column(name = "remaining_sms", nullable = false)
    private Integer remainingSms;

    public SmsBalance(String externalCustomerId, Integer remainingSms) {
        this.externalCustomerId = externalCustomerId;
        this.remainingSms = remainingSms;
    }

    // Use SMS credits
    public boolean useSms(int count) {
        if (remainingSms >= count) {
            this.remainingSms -= count;
            return true;
        }
        return false;
    }

    // Recharge SMS credits (synced from external provider)
    public void recharge(int credits) {
        this.remainingSms += credits;
    }
}

