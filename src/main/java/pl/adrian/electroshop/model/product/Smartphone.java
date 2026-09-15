package pl.adrian.electroshop.model.product;

import lombok.Getter;
import lombok.NonNull;
import pl.adrian.electroshop.exception.InvalidProductConfigurationException;
import pl.adrian.electroshop.model.product.configuration.ProductConfiguration;
import pl.adrian.electroshop.model.product.configuration.SmartphoneConfiguration;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public class Smartphone extends Product {
    private final List<String> availableColors;
    private final List<Integer> availableBatteryCapacities;
    private final List<String> availableAccessories;

    public Smartphone(String id, String name, BigDecimal price, int quantity,
                      @NonNull List<String> availableColors,
                      @NonNull List<Integer> availableBatteryCapacities,
                      @NonNull List<String> availableAccessories) {

        super(id, name, price, quantity);

        this.availableColors = List.copyOf(availableColors);
        this.availableBatteryCapacities = List.copyOf(availableBatteryCapacities);
        this.availableAccessories = List.copyOf(availableAccessories);
    }

    @Override
    public void validateConfiguration(ProductConfiguration configuration) {
        if (!(configuration instanceof SmartphoneConfiguration sc)) {
            throw new InvalidProductConfigurationException("Invalid configuration type for Smartphone: " + getId());
        }
        if (!availableColors.contains(sc.color())) {
            throw new InvalidProductConfigurationException("Color " + sc.color() + " is not available for this smartphone model.");
        }
        if (!availableBatteryCapacities.contains(sc.batteryCapacity())) {
            throw new InvalidProductConfigurationException("Battery capacity " + sc.batteryCapacity() + " mAh is not available.");
        }
        for (String accessory : sc.accessories()) {
            if (!availableAccessories.contains(accessory)) {
                throw new InvalidProductConfigurationException("Accessory " + accessory + " is not available for this smartphone model.");
            }
        }
    }

    @Override
    public String toString() {
        return String.format("Smartphone [ID: %s, Name: %s, Price: %.2f zł, Quantity: %d] ",
                getId(), getName(), getPrice(), getQuantity());
    }

    // Design Pattern: BUILDER (creational, GoF)
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private String name;
        private BigDecimal price;
        private int quantity;
        private List<String> availableColors = List.of();
        private List<Integer> availableBatteryCapacities = List.of();
        private List<String> availableAccessories = List.of();

        private Builder() {
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder price(BigDecimal price) {
            this.price = price;
            return this;
        }

        public Builder quantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder colors(List<String> colors) {
            this.availableColors = colors;
            return this;
        }

        public Builder batteryCapacities(List<Integer> capacities) {
            this.availableBatteryCapacities = capacities;
            return this;
        }

        public Builder accessories(List<String> accessories) {
            this.availableAccessories = accessories;
            return this;
        }

        public Smartphone build() {
            Objects.requireNonNull(id, "id is required");
            Objects.requireNonNull(name, "name is required");
            Objects.requireNonNull(price, "price is required");
            return new Smartphone(id, name, price, quantity,
                    availableColors, availableBatteryCapacities, availableAccessories);
        }
    }
}