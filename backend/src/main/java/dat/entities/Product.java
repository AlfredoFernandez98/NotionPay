package dat.entities;

import dat.enums.Currency;
import dat.enums.ProductType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Product {
    @Id
    @GeneratedValue
    @Column(name = "product_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false)
    private ProductType productType;

    @Column(nullable = false)
    private String name;

    @Column(name = "price_cents", nullable = false)
    private Integer priceCents;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    private String description;

    public Product(ProductType productType, String name, Integer priceCents, Currency currency, String description) {
        this.productType = productType;
        this.name = name;
        this.priceCents = priceCents;
        this.currency = currency;
        this.description = description;
    }
}

