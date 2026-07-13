package pl.adrian.electroshop.model;

import java.math.BigDecimal;

public class Electronics extends Product {

    public Electronics(String id, String name, BigDecimal price, int quantity) {
        super(id, name, price, quantity);
    }

    @Override
    public String toString() {
        return String.format("Electronics [ID: %s, Name: %s, Price: %.2f zł, Quantity: %d]",
                getId(), getName(), getPrice(), getQuantity());
    }
}