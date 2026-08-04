package pl.adrian.electroshop.model.product;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import pl.adrian.electroshop.model.product.configuration.ProductConfiguration;

import java.math.BigDecimal;

@Getter
public abstract class Product {
    private final String id;
    @Setter private String name;
    @Setter private BigDecimal price;
    @Setter private int quantity;

    public Product(@NonNull String id,
                   @NonNull String name,
                   @NonNull BigDecimal price,
                   int quantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public abstract void validateConfiguration(ProductConfiguration configuration);

    // Jedyny sposób na utworzenie CartItem
    public CartItem toCartItem(ProductConfiguration configuration, int quantity) {
        validateConfiguration(configuration);
        return new CartItem(id, name, price, configuration, quantity);
    }
}
