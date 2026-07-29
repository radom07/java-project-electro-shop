package pl.adrian.electroshop.model.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.adrian.electroshop.exception.InvalidProductConfigurationException;
import pl.adrian.electroshop.model.product.configuration.ComputerConfiguration;
import pl.adrian.electroshop.model.product.configuration.NoConfiguration;
import pl.adrian.electroshop.model.product.configuration.SmartphoneConfiguration;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class ComputerTest {

    private List<String> cpus;
    private List<Integer> rams;
    private Computer computer;

    @BeforeEach
    void setUp() {
        // given
        cpus = List.of("Intel i5", "Intel i7");
        rams = List.of(8, 16, 32);
        computer = new Computer("C1", "Dell XPS", new BigDecimal("4999.99"), 10, cpus, rams);
    }

    @Test
    void shouldCreateComputerWithCatalogData() {
        // then
        assertThat(computer.getId()).isEqualTo("C1");
        assertThat(computer.getName()).isEqualTo("Dell XPS");
        assertThat(computer.getPrice()).isEqualByComparingTo("4999.99");
        assertThat(computer.getQuantity()).isEqualTo(10);
        assertThat(computer.getAvailableCpus()).containsExactly("Intel i5", "Intel i7");
        assertThat(computer.getAvailableRamOptions()).containsExactly(8, 16, 32);
    }

    @Test
    void shouldNotThrowWhenValidatingCorrectConfiguration() {
        // when & then
        assertThatCode(() -> computer.validateConfiguration(new ComputerConfiguration("Intel i7", 16)))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldThrowExceptionWhenValidatingInvalidCpu() {
        // when & then
        assertThatThrownBy(() -> computer.validateConfiguration(new ComputerConfiguration("AMD Ryzen 9", 16)))
                .isInstanceOf(InvalidProductConfigurationException.class)
                .hasMessageContaining("Processor AMD Ryzen 9 is not available");
    }

    @Test
    void shouldThrowExceptionWhenValidatingInvalidRam() {
        // when & then
        assertThatThrownBy(() -> computer.validateConfiguration(new ComputerConfiguration("Intel i7", 64)))
                .isInstanceOf(InvalidProductConfigurationException.class)
                .hasMessageContaining("Amount of RAM 64 GB is not available");
    }

    @Test
    void shouldThrowExceptionWhenConfigurationTypeDoesNotMatch() {
        // when & then
        assertThatThrownBy(() -> computer.validateConfiguration(new NoConfiguration()))
                .isInstanceOf(InvalidProductConfigurationException.class)
                .hasMessageContaining("Invalid configuration type for Computer");

        assertThatThrownBy(() -> computer.validateConfiguration(
                new SmartphoneConfiguration("Black", 4000, List.of())))
                .isInstanceOf(InvalidProductConfigurationException.class)
                .hasMessageContaining("Invalid configuration type for Computer");
    }

    @Test
    void shouldCreateCartItemWhenConfigurationIsValid() {
        // given
        var cartItem = computer.toCartItem(new ComputerConfiguration("Intel i7", 16), 2);

        // then
        assertThat(cartItem.getProductId()).isEqualTo("C1");
        assertThat(cartItem.getProductName()).isEqualTo("Dell XPS");
        assertThat(cartItem.getUnitPrice()).isEqualByComparingTo("4999.99");
        assertThat(cartItem.getConfiguration()).isEqualTo(new ComputerConfiguration("Intel i7", 16));
        assertThat(cartItem.getQuantity()).isEqualTo(2);
    }

    @Test
    void shouldNotCreateCartItemWhenConfigurationIsInvalid() {
        // when & then
        assertThatThrownBy(() -> computer.toCartItem(new ComputerConfiguration("AMD Ryzen 9", 16), 1))
                .isInstanceOf(InvalidProductConfigurationException.class);
    }
}