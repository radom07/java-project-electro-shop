package pl.adrian.electroshop.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class ComputerTest {

    private List<String> cpus;
    private List<Integer> rams;
    private Computer computer;

    @BeforeEach
    void setUp() {
        // Dane testowe
        cpus = List.of("Intel i5", "Intel i7");
        rams = List.of(8, 16, 32);
        computer = new Computer("C1", "Dell XPS", new BigDecimal("4999.99"), 10, cpus, rams);
    }

    @Test
    void shouldCreateComputerWithDefaultConfiguration() {
        assertThat(computer.getId()).isEqualTo("C1");
        assertThat(computer.getName()).isEqualTo("Dell XPS");
        assertThat(computer.getPrice()).isEqualByComparingTo("4999.99");
        assertThat(computer.getQuantity()).isEqualTo(10);
        assertThat(computer.getSelectedCpu()).isEqualTo("Intel i5");
        assertThat(computer.getSelectedRam()).isEqualTo(8);
    }

    @Test
    void shouldConfigureComputerWithValidParameters() {
        // when
        computer.configure("Intel i7", 16);

        // then
        assertThat(computer.getSelectedCpu()).isEqualTo("Intel i7");
        assertThat(computer.getSelectedRam()).isEqualTo(16);
    }

    @Test
    void shouldThrowExceptionWhenConfiguringWithInvalidCpu() {
        // when & then
        assertThatThrownBy(() -> computer.configure("AMD Ryzen 9", 16))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Processor AMD Ryzen 9 is not available");

        assertThat(computer.getSelectedCpu()).isEqualTo("Intel i5");
    }

    @Test
    void shouldThrowExceptionWhenConfiguringWithInvalidRam() {
        // when & then
        assertThatThrownBy(() -> computer.configure("Intel i7", 64))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Amount of RAM 64 GB is not available");

        assertThat(computer.getSelectedRam()).isEqualTo(8);
    }
}