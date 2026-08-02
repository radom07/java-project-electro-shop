package pl.adrian.electroshop.model.product;

import lombok.Getter;
import lombok.NonNull;
import pl.adrian.electroshop.exception.InvalidProductConfigurationException;
import pl.adrian.electroshop.model.product.configuration.ComputerConfiguration;
import pl.adrian.electroshop.model.product.configuration.ProductConfiguration;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class Computer extends Product {
    private final List<String> availableCpus;
    private final List<Integer> availableRamOptions; // GB

    public Computer(String id, String name, BigDecimal price, int quantity,
                    @NonNull List<String> availableCpus,
                    @NonNull List<Integer> availableRamOptions) {

        super(id, name, price, quantity);

        this.availableCpus = availableCpus;
        this.availableRamOptions = availableRamOptions;
    }

    @Override
    public void validateConfiguration(ProductConfiguration configuration) {
        if (!(configuration instanceof ComputerConfiguration cc)) {
            throw new InvalidProductConfigurationException("Invalid configuration type for Computer: " + getId());
        }
        if (!availableCpus.contains(cc.cpu())) {
            throw new InvalidProductConfigurationException("Processor " + cc.cpu() + " is not available for this computer model.");
        }
        if (!availableRamOptions.contains(cc.ram())) {
            throw new InvalidProductConfigurationException("Amount of RAM " + cc.ram() + " GB is not available for this computer model.");
        }
    }

    @Override
    public String toString() {
        return String.format("Computer [ID: %s, Name: %s, Price: %.2f zł, Quantity: %d]",
                getId(), getName(), getPrice(), getQuantity());
    }
}