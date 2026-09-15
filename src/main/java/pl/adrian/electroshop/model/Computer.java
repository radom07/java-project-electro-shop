package pl.adrian.electroshop.model;

import java.math.BigDecimal;
import java.util.List;

public class Computer extends Product {
    // Dwie listy z dostępnymi opcjami konfiguracji komputera
    private final List<String> availableCpus;
    private final List<Integer> availableRamOptions; // GB

    // Dwa dodatkowe pola względem klasy Product - konfiguracja wybrana przez klienta
    private String selectedCpu;
    private int selectedRam;

    public Computer(String id, String name, BigDecimal price, int quantity,
                    List<String> availableCpus, List<Integer> availableRamOptions) {

        super(id, name, price, quantity);

        this.availableCpus = availableCpus;
        this.availableRamOptions = availableRamOptions;

        // Ustawiam default konfiguracji
        this.selectedCpu = availableCpus.isEmpty() ? "Standard CPU" : availableCpus.get(0);
        this.selectedRam = availableRamOptions.isEmpty() ? 8 : availableRamOptions.get(0);
    }

    // Metoda umożliwiająca konfigurację specyfikacji przed dodaniem do koszyka
    public void configure(String cpu, int ram) {
        if (!availableCpus.contains(cpu)) {
            throw new IllegalArgumentException("Processor " + cpu + " is not available for this computer model.");
        }
        if (!availableRamOptions.contains(ram)) {
            throw new IllegalArgumentException("Amount of RAM " + ram + " GB is not available for this computer model.");
        }

        this.selectedCpu = cpu;
        this.selectedRam = ram;
    }

    public String getSelectedCpu() {
        return selectedCpu;
    }

    public int getSelectedRam() {
        return selectedRam;
    }

    public List<String> getAvailableCpus() {
        return availableCpus;
    }

    public List<Integer> getAvailableRamOptions() {
        return availableRamOptions;
    }

    @Override
    public String toString() {
        return String.format("Computer [ID: %s, Name: %s, Price: %.2f zł, Quantity: %d] " +
                        "(CPU: %s, RAM: %d GB)",
                getId(), getName(), getPrice(), getQuantity(), selectedCpu, selectedRam);
    }
}