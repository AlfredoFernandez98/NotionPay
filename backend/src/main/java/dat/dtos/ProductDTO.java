package dat.dtos;

import dat.enums.Currency;
import dat.enums.ProductType;

public class ProductDTO {
    public Long id;
    public ProductType productType;
    public String name;
    public Integer priceCents;
    public Currency currency;
    public String description;
    public Integer smsCount;
}

