package pl.adrian.electroshop.model.product;

import org.junit.jupiter.api.Test;
import pl.adrian.electroshop.exception.InvalidProductConfigurationException;
import pl.adrian.electroshop.model.product.configuration.ComputerConfiguration;
import pl.adrian.electroshop.model.product.configuration.NoConfiguration;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ElectronicsTest {

    @Test
    void shouldCreateStandardElectronicsProduct() {
        // given
        Electronics cable = new Electronics("E1", "USB-C Cable", new BigDecimal("49.99"), 100);

        // then
        assertThat(cable.getId()).isEqualTo("E1");
        assertThat(cable.getName()).isEqualTo("USB-C Cable");
        assertThat(cable.getPrice()).isEqualByComparingTo("49.99");
        assertThat(cable.getQuantity()).isEqualTo(100);
        assertThat(cable.toString()).contains("USB-C Cable", "49,99");
    }

    @Test
    void shouldNotThrowWhenValidatingNoConfiguration() {
        // given
        Electronics cable = new Electronics("E1", "USB-C Cable", new BigDecimal("49.99"), 100);

        // when & then
        assertThatCode(() -> cable.validateConfiguration(new NoConfiguration()))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldThrowExceptionWhenValidatingAnyOtherConfigurationType() {
        // given
        Electronics cable = new Electronics("E1", "USB-C Cable", new BigDecimal("49.99"), 100);

        // when & then
        assertThatThrownBy(() -> cable.validateConfiguration(new ComputerConfiguration("Intel i5", 8)))
                .isInstanceOf(InvalidProductConfigurationException.class)
                .hasMessageContaining("Electronics product does not support configuration");
    }

    @Test
    void shouldCreateCartItemWithNoConfiguration() {
        // given
        Electronics cable = new Electronics("E1", "USB-C Cable", new BigDecimal("49.99"), 100);

        // when
        var cartItem = cable.toCartItem(new NoConfiguration(), 3);

        // then
        assertThat(cartItem.getProductId()).isEqualTo("E1");
        assertThat(cartItem.getQuantity()).isEqualTo(3);
        assertThat(cartItem.getUnitPrice()).isEqualByComparingTo("49.99");
    }
}