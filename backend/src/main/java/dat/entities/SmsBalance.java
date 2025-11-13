package dat.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

/**
 * SmsBalance Entity - Simulates External SMS Provider Database
 * Linked to Customer via external_customer_id (not customer_id)
 * This represents SMS credits from an external SMS service provider
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "sms_balances")
public class SmsBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link to Customer via external_customer_id (from external payment system)
    @Column(name = "external_customer_id", unique = true, nullable = false)
    private String externalCustomerId;  // Links to Customer.external_customer_id

    @Column(name = "total_sms_credits", nullable = false)
    private Integer totalSmsCredits;  // Total SMS purchased

    @Column(name = "used_sms_credits", nullable = false)
    private Integer usedSmsCredits;   // SMS already sent

    @Column(name = "remaining_sms_credits", nullable = false)
    private Integer remainingSmsCredits;  // SMS left to use

    @Column(name = "last_recharged_at")
    private OffsetDateTime lastRechargedAt;  // When last topped up

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    // Constructor for creating new SMS balance
    public SmsBalance(String externalCustomerId, Integer totalSmsCredits) {
        this.externalCustomerId = externalCustomerId;
        this.totalSmsCredits = totalSmsCredits;
        this.usedSmsCredits = 0;
        this.remainingSmsCredits = totalSmsCredits;
        this.createdAt = OffsetDateTime.now();
        this.lastRechargedAt = OffsetDateTime.now();
    }

    // Method to use SMS credits
    public boolean useSms(int count) {
        if (remainingSmsCredits >= count) {
            this.usedSmsCredits += count;
            this.remainingSmsCredits -= count;
            this.updatedAt = OffsetDateTime.now();
            return true;
        }
        return false;
    }

    // Method to recharge SMS credits
    public void recharge(int credits) {
        this.totalSmsCredits += credits;
        this.remainingSmsCredits += credits;
        this.lastRechargedAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }
}

