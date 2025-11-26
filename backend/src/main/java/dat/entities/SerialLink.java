package dat.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

/**
 * Lookup table linking serial numbers to external customer data
 * Represents data synced from external system (like Netflix's internal DB)
 * Used during registration to verify and onboard customers to payment system
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class SerialLink {

    @Id
    @GeneratedValue
    private Long id;
    
    @Column(name = "serial_number", unique = true, nullable = false)
    private Integer serialNumber;

    @Column(name = "external_customer_id", unique = true, nullable = false)
    private String externalCustomerId;

    @Column(name = "expected_email", nullable = false)
    private String expectedEmail;
    
    @Column(name = "plan_name", nullable = false)
    private String planName;
    
    @Column(name = "initial_sms_balance", nullable = false)
    private Integer initialSmsBalance;
    
    @Column(name = "next_payment_date", nullable = false)
    private OffsetDateTime nextPaymentDate; // When external system expects next payment

    public SerialLink(Integer serialNumber, String externalCustomerId, String expectedEmail, 
                     String planName, Integer initialSmsBalance, OffsetDateTime nextPaymentDate) {
        this.serialNumber = serialNumber;
        this.externalCustomerId = externalCustomerId;
        this.expectedEmail = expectedEmail;
        this.planName = planName;
        this.initialSmsBalance = initialSmsBalance;
        this.nextPaymentDate = nextPaymentDate;
    }
}
