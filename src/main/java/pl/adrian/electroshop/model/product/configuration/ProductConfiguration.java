package pl.adrian.electroshop.model.product.configuration;

public sealed interface ProductConfiguration permits ComputerConfiguration, SmartphoneConfiguration, NoConfiguration {
}
