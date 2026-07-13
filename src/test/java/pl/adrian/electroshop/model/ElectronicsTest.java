package pl.adrian.electroshop.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

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
}