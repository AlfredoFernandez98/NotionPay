package dat.dtos;

import dat.enums.Currency;
import dat.enums.Period;

public class PlanDTO {
    public Long id;
    public String name;
    public Period period;
    public Double priceCents;
    public Currency currency;
    public String description;
    public Boolean active;
}
