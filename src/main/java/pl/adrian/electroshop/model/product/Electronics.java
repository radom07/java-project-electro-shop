package pl.adrian.electroshop.model.product;

import pl.adrian.electroshop.exception.InvalidProductConfigurationException;
import pl.adrian.electroshop.model.product.configuration.NoConfiguration;
import pl.adrian.electroshop.model.product.configuration.ProductConfiguration;

import java.math.BigDecimal;

public class Electronics extends Product {

    public Electronics(String id, String name, BigDecimal price, int quantity) {
        super(id, name, price, quantity);
    }

    @Override
    public void validateConfiguration(ProductConfiguration configuration) {
        if (!(configuration instanceof NoConfiguration)) {
            throw new InvalidProductConfigurationException("Electronics product does not support configuration: " + getId());
        }
    }

    @Override
    public String toString() {
        return String.format("Electronics [ID: %s, Name: %s, Price: %.2f zł, Quantity: %d]",
                getId(), getName(), getPrice(), getQuantity());
    }
}