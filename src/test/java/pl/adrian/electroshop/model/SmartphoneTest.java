package pl.adrian.electroshop.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SmartphoneTest {

    private List<String> colors;
    private List<Integer> batteries;
    private List<String> accessories;
    private Smartphone smartphone;

    @BeforeEach
    void setUp() {
        // Dane testowe
        colors = List.of("Black", "Silver");
        batteries = List.of(4000, 5000);
        accessories = List.of("Case", "Glass");

        smartphone = new Smartphone(
                "S1",
                "iPhone 15",
                new BigDecimal("3999.00"),
                15,
                colors,
                batteries,
                accessories
        );
    }

    @Test
    void shouldCreateSmartphoneWithDefaultConfiguration() {
        assertThat(smartphone.getId()).isEqualTo("S1");
        assertThat(smartphone.getPrice()).isEqualByComparingTo("3999.00");
        assertThat(smartphone.getSelectedColor()).isEqualTo("Black");
        assertThat(smartphone.getSelectedBatteryCapacity()).isEqualTo(4000);
        assertThat(smartphone.getSelectedAccessories()).isEmpty();
    }

    @Test
    void shouldConfigureSmartphoneWithValidParameters() {
        smartphone.configure("Silver", 5000);

        assertThat(smartphone.getSelectedColor()).isEqualTo("Silver");
        assertThat(smartphone.getSelectedBatteryCapacity()).isEqualTo(5000);
    }

    @Test
    void shouldThrowExceptionWhenConfiguringWithInvalidColor() {
        assertThatThrownBy(() -> smartphone.configure("Gold", 5000))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Color Gold is not available");
    }

    @Test
    void shouldAddAvailableAccessories() {
        smartphone.addAccessory("Case");
        smartphone.addAccessory("Glass");

        assertThat(smartphone.getSelectedAccessories())
                .hasSize(2)
                .containsExactly("Case", "Glass");
    }

    @Test
    void shouldThrowExceptionWhenAddingUnavailableAccessory() {
        assertThatThrownBy(() -> smartphone.addAccessory("Wireless Charger"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Accessory Wireless Charger is not available");
    }

    @Test
    void shouldThrowExceptionWhenAddingDuplicateAccessory() {
        smartphone.addAccessory("Case");

        assertThatThrownBy(() -> smartphone.addAccessory("Case"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Accessory Case have already been selected");
    }
}