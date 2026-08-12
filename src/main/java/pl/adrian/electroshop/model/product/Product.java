package pl.adrian.electroshop.model.product;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import pl.adrian.electroshop.exception.InsufficientStockException;
import pl.adrian.electroshop.model.product.configuration.ProductConfiguration;

import java.math.BigDecimal;

@Getter
public abstract class Product {
    private final String id;
    @Setter
    private String name;
    @Setter
    private BigDecimal price;
    private int quantity;

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

    public void decreaseStock(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must not be negative");
        }
        if (amount > quantity) {
            throw new InsufficientStockException(id);
        }
        quantity -= amount;
    }

    public void increaseStock(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must not be negative");
        }
        quantity += amount;
    }
}
