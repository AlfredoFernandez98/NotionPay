package dat.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class SmsBalance {
    @Id
    @GeneratedValue
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "customer_id", nullable = false, unique = true)
    private Customer customer;

    @Column(name = "remaining_sms", nullable = false)
    private Integer remainingSms;

    public SmsBalance(Customer customer, Integer remainingSms) {
        this.customer = customer;
        this.remainingSms = remainingSms;
    }
}

