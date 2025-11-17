package dat.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lookup table linking serial numbers to external customer IDs and initial SMS balance
 * Used during registration to validate and provide initial credits
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
    private Integer initialSmsBalance; // Fake SMS balance the customer gets

    public SerialLink(Integer serialNumber, String externalCustomerId, String expectedEmail, 
                     String planName, Integer initialSmsBalance) {
        this.serialNumber = serialNumber;
        this.externalCustomerId = externalCustomerId;
        this.expectedEmail = expectedEmail;
        this.planName = planName;
        this.initialSmsBalance = initialSmsBalance;
    }
}
