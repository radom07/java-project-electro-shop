package pl.adrian.electroshop.model.product.configuration;

import java.util.List;

public record SmartphoneConfiguration(String color, int batteryCapacity, List<String> accessories)
        implements ProductConfiguration {
}
