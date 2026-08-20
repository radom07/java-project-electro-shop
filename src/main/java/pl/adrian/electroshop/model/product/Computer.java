package pl.adrian.electroshop.model.product;

import lombok.Getter;
import lombok.NonNull;
import pl.adrian.electroshop.exception.InvalidProductConfigurationException;
import pl.adrian.electroshop.model.product.configuration.ComputerConfiguration;
import pl.adrian.electroshop.model.product.configuration.ProductConfiguration;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Getter
public class Computer extends Product {
    private final List<String> availableCpus;
    private final List<Integer> availableRamOptions;

    public Computer(String id, String name, BigDecimal price, int quantity,
                    @NonNull List<String> availableCpus,
                    @NonNull List<Integer> availableRamOptions) {

        super(id, name, price, quantity);

        this.availableCpus = List.copyOf(availableCpus);
        this.availableRamOptions = List.copyOf(availableRamOptions);
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

    // Design Pattern: BUILDER (creational, GoF)
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private String name;
        private BigDecimal price;
        private int quantity;
        private List<String> availableCpus = List.of();
        private List<Integer> availableRamOptions = List.of();

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

        public Builder cpus(List<String> cpus) {
            this.availableCpus = cpus;
            return this;
        }

        public Builder ramOptions(List<Integer> ramOptions) {
            this.availableRamOptions = ramOptions;
            return this;
        }

        public Computer build() {
            Objects.requireNonNull(id, "id is required");
            Objects.requireNonNull(name, "name is required");
            Objects.requireNonNull(price, "price is required");
            return new Computer(id, name, price, quantity, availableCpus, availableRamOptions);
        }
    }
}