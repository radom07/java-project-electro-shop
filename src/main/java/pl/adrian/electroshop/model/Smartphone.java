package pl.adrian.electroshop.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Smartphone extends Product {
    // Trzy listy z dostępnymi opcjami konfiguracji smartfona
    private final List<String> availableColors;
    private final List<Integer> availableBatteryCapacities; // mAh
    private final List<String> availableAccessories; // "Case", "Glass", "Charger", "Phone Holder"

    // Trzy dodatkowe pola względem klasy Product - konfiguracja wybrana przez klienta
    private String selectedColor;
    private int selectedBatteryCapacity;
    private final List<String> selectedAccessories; // Można wybrać kilka

    public Smartphone(String id, String name, BigDecimal price, int quantity,
                      List<String> availableColors, List<Integer> availableBatteryCapacities,
                      List<String> availableAccessories) {

        super(id, name, price, quantity);

        this.availableColors = availableColors;
        this.availableBatteryCapacities = availableBatteryCapacities;
        this.availableAccessories = availableAccessories;

        // Ustawiam default konfiguracji
        this.selectedColor = availableColors.isEmpty() ? "Black" : availableColors.get(0);
        this.selectedBatteryCapacity = availableBatteryCapacities.isEmpty() ? 4000 : availableBatteryCapacities.get(0);
        this.selectedAccessories = new ArrayList<>();
    }

    // Metoda umożliwiająca konfigurację koloru i pojemności baterii przed dodaniem do koszyka
    public void configure(String color, int batteryCapacity) {
        if (!availableColors.contains(color)) {
            throw new IllegalArgumentException("Color " + color + " is not available for this smartphone model.");
        }
        if (!availableBatteryCapacities.contains(batteryCapacity)) {
            throw new IllegalArgumentException("Battery capacity " + batteryCapacity + " mAh is not available for " +
                    "this smartphone model.");
        }
        this.selectedColor = color;
        this.selectedBatteryCapacity = batteryCapacity;
    }

    // Metoda umożliwiająca wybranie akcesoriów
    public void addAccessory(String accessory) {
        if (!availableAccessories.contains(accessory)) {
            throw new IllegalArgumentException("Accessory " + accessory + " is not available for this smartphone model.");
        }
        if (selectedAccessories.contains(accessory)) {
            throw new IllegalStateException("Accessory " + accessory + " have already been selected.");
        }
        selectedAccessories.add(accessory);
    }

    // Gettery
    public String getSelectedColor() {
        return selectedColor;
    }

    public int getSelectedBatteryCapacity() {
        return selectedBatteryCapacity;
    }

    public List<String> getSelectedAccessories() {
        return new ArrayList<>(selectedAccessories); // Kopia listy
    }

    @Override
    public String toString() {
        return String.format("Smartphone [ID: %s, Name: %s, Price: %.2f zł, Quantity: %d] " +
                        "(Color: %s, Battery: %d mAh, Accessories: %s)",
                getId(), getName(), getPrice(), getQuantity(), selectedColor, selectedBatteryCapacity, selectedAccessories);
    }
}