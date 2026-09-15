package pl.adrian.electroshop.model.product;

import pl.adrian.electroshop.model.product.configuration.ComputerConfiguration;
import pl.adrian.electroshop.model.product.configuration.ProductConfiguration;

import java.math.BigDecimal;
import java.util.List;

public class Computer extends Product {
    private final List<String> availableCpus;
    private final List<Integer> availableRamOptions; // GB

    public Computer(String id, String name, BigDecimal price, int quantity,
                    List<String> availableCpus, List<Integer> availableRamOptions) {

        super(id, name, price, quantity);

        this.availableCpus = availableCpus;
        this.availableRamOptions = availableRamOptions;
    }

    public List<String> getAvailableCpus() {
        return availableCpus;
    }

    public List<Integer> getAvailableRamOptions() {
        return availableRamOptions;
    }

    @Override
    public void validateConfiguration(ProductConfiguration configuration) {
        if (!(configuration instanceof ComputerConfiguration cc)) {
            throw new IllegalArgumentException("Invalid configuration type for Computer: " + getId());
        }
        if (!availableCpus.contains(cc.cpu())) {
            throw new IllegalArgumentException("Processor " + cc.cpu() + " is not available for this computer model.");
        }
        if (!availableRamOptions.contains(cc.ram())) {
            throw new IllegalArgumentException("Amount of RAM " + cc.ram() + " GB is not available for this computer model.");
        }
    }

    @Override
    public String toString() {
        return String.format("Computer [ID: %s, Name: %s, Price: %.2f zł, Quantity: %d]",
                getId(), getName(), getPrice(), getQuantity());
    }
}