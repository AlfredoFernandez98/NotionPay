package dat.entities;

import dat.enums.Currency;
import dat.enums.Period;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Getter
@Setter
@NoArgsConstructor
public class Plan {
    @Id
    @GeneratedValue
    private Long id;
    private String name;

    @Enumerated(EnumType.STRING)
    private Period period;
    private double priceCents;

    @Enumerated(EnumType.STRING)
    private Currency currency;
    private String description;
    private Boolean active;

    public Plan(String name, Period period, double priceCents, Currency currency, String description, Boolean active) {
        this.name = name;
        this.period = period;
        this.priceCents = priceCents;
        this.currency = currency;
        this.description = description;
        this.active = active;
    }
}
