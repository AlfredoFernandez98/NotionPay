package dat.entities;

import dat.enums.Currency;
import dat.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Payment {
    @Id
    @GeneratedValue
    @Column(name = "payment_id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(optional = true)
    @JoinColumn(name = "payment_method_id", nullable = true)
    private PaymentMethod paymentMethod;

    @ManyToOne(optional = true)
    @JoinColumn(name = "subscription_id", nullable = true)
    private Subscription subscription;

    @ManyToOne(optional = true)
    @JoinColumn(name = "product_id", nullable = true)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(name = "price_cents", nullable = false)
    private Integer priceCents;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    @Column(name = "processor_intent_id")
    private String processorIntentId;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    public Payment(Customer customer, PaymentMethod paymentMethod, Subscription subscription, 
                  Product product, PaymentStatus status, Integer priceCents, Currency currency, 
                  String processorIntentId) {
        this.customer = customer;
        this.paymentMethod = paymentMethod;
        this.subscription = subscription;
        this.product = product;
        this.status = status;
        this.priceCents = priceCents;
        this.currency = currency;
        this.processorIntentId = processorIntentId;
        this.createdAt = OffsetDateTime.now();
    }
}

