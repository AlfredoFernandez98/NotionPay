package dat.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class SmsProduct {
    @Id
    @GeneratedValue
    @Column(name = "sms_product_id")
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "sms_count", nullable = false)
    private Integer smsCount;

    public SmsProduct(Product product, Integer smsCount) {
        this.product = product;
        this.smsCount = smsCount;
    }
}

