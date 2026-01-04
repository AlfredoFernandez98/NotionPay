package dat.entities;

import dat.enums.PaymentMethodStatus;
import dat.utils.DateTimeUtil;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class PaymentMethod {
    @Id
    @GeneratedValue
    @Column(name = "payment_method_id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false)
    private String type;

    private String brand;

    private String last4;

    @Column(name = "exp_month")
    private Integer expMonth;

    @Column(name = "exp_year")
    private Integer expYear;

    @Column(name = "processor_method_id", unique = true)
    private String processorMethodId;

    @Column(name = "is_default")
    private Boolean isDefault;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethodStatus status;

    private String fingerprint;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public PaymentMethod(Customer customer, String type, String brand, String last4, 
                        Integer expMonth, Integer expYear, String processorMethodId, 
                        Boolean isDefault, PaymentMethodStatus status, String fingerprint) {
        this.customer = customer;
        this.type = type;
        this.brand = brand;
        this.last4 = last4;
        this.expMonth = expMonth;
        this.expYear = expYear;
        this.processorMethodId = processorMethodId;
        this.isDefault = isDefault;
        this.status = status;
        this.fingerprint = fingerprint;
        this.createdAt = DateTimeUtil.now();
        this.updatedAt = DateTimeUtil.now();
    }
}

