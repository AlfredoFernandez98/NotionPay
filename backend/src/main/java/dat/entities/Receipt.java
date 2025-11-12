package dat.entities;

import dat.enums.ReceiptStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Receipt {
    @Id
    @GeneratedValue
    @Column(name = "receipt_id")
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(name = "receipt_number", unique = true, nullable = false)
    private String receiptNumber;

    @Column(name = "price_cents", nullable = false)
    private Integer priceCents;

    @Column(name = "paid_at")
    private OffsetDateTime paidAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReceiptStatus status;

    @Column(name = "processor_receipt_url")
    private String processorReceiptUrl;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "pm_brand")
    private String pmBrand;

    @Column(name = "pm_last4")
    private String pmLast4;

    @Column(name = "pm_exp_year")
    private Integer pmExpYear;

    @Column(name = "processor_intent_id")
    private String processorIntentId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    public Receipt(Payment payment, String receiptNumber, Integer priceCents, OffsetDateTime paidAt,
                  ReceiptStatus status, String processorReceiptUrl, String customerEmail, 
                  String companyName, String pmBrand, String pmLast4, Integer pmExpYear, 
                  String processorIntentId, Map<String, Object> metadata) {
        this.payment = payment;
        this.receiptNumber = receiptNumber;
        this.priceCents = priceCents;
        this.paidAt = paidAt;
        this.status = status;
        this.processorReceiptUrl = processorReceiptUrl;
        this.customerEmail = customerEmail;
        this.companyName = companyName;
        this.pmBrand = pmBrand;
        this.pmLast4 = pmLast4;
        this.pmExpYear = pmExpYear;
        this.processorIntentId = processorIntentId;
        this.metadata = metadata;
        this.createdAt = OffsetDateTime.now();
    }
}

