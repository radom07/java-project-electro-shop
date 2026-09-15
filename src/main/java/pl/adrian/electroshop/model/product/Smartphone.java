package pl.adrian.electroshop.model.product;

import pl.adrian.electroshop.exception.InvalidProductConfigurationException;
import pl.adrian.electroshop.model.product.configuration.ProductConfiguration;
import pl.adrian.electroshop.model.product.configuration.SmartphoneConfiguration;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Smartphone extends Product {
    private final List<String> availableColors;
    private final List<Integer> availableBatteryCapacities; // mAh
    private final List<String> availableAccessories; // "Case", "Glass", "Charger", "Phone Holder"

    public Smartphone(String id, String name, BigDecimal price, int quantity,
                      List<String> availableColors, List<Integer> availableBatteryCapacities,
                      List<String> availableAccessories) {

        super(id, name, price, quantity);

        this.availableColors = availableColors;
        this.availableBatteryCapacities = availableBatteryCapacities;
        this.availableAccessories = availableAccessories;
    }

    public List<String> getAvailableColors() {
        return availableColors;
    }

    public List<Integer> getAvailableBatteryCapacities() {
        return availableBatteryCapacities;
    }

    public List<String> getAvailableAccessories() {
        return availableAccessories;
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
}