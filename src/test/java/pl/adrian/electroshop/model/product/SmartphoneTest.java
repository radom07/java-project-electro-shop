package pl.adrian.electroshop.model.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.adrian.electroshop.exception.InvalidProductConfigurationException;
import pl.adrian.electroshop.model.product.configuration.NoConfiguration;
import pl.adrian.electroshop.model.product.configuration.SmartphoneConfiguration;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SmartphoneTest {

    private List<String> colors;
    private List<Integer> batteries;
    private List<String> accessories;
    private Smartphone smartphone;

    @BeforeEach
    void setUp() {
        // given
        colors = List.of("Black", "Silver");
        batteries = List.of(4000, 5000);
        accessories = List.of("Case", "Glass");

        smartphone = new Smartphone(
                "S1", "iPhone 15", new BigDecimal("3999.00"), 15,
                colors, batteries, accessories
        );
    }

    @Test
    void shouldCreateSmartphoneWithCatalogData() {
        // then
        assertThat(smartphone.getId()).isEqualTo("S1");
        assertThat(smartphone.getPrice()).isEqualByComparingTo("3999.00");
        assertThat(smartphone.getAvailableColors()).containsExactly("Black", "Silver");
        assertThat(smartphone.getAvailableBatteryCapacities()).containsExactly(4000, 5000);
        assertThat(smartphone.getAvailableAccessories()).containsExactly("Case", "Glass");
    }

    @Test
    void shouldNotThrowWhenValidatingCorrectConfiguration() {
        // given
        var configuration = new SmartphoneConfiguration("Silver", 5000, List.of("Case"));

        // when & then
        assertThatCode(() -> smartphone.validateConfiguration(configuration))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldThrowExceptionWhenValidatingInvalidColor() {
        // given
        var configuration = new SmartphoneConfiguration("Gold", 5000, List.of());

        // when & then
        assertThatThrownBy(() -> smartphone.validateConfiguration(configuration))
                .isInstanceOf(InvalidProductConfigurationException.class)
                .hasMessageContaining("Color Gold is not available");
    }

    @Test
    void shouldThrowExceptionWhenValidatingInvalidBatteryCapacity() {
        // given
        var configuration = new SmartphoneConfiguration("Black", 6000, List.of());

        // when & then
        assertThatThrownBy(() -> smartphone.validateConfiguration(configuration))
                .isInstanceOf(InvalidProductConfigurationException.class)
                .hasMessageContaining("Battery capacity 6000 mAh is not available");
    }

    @Test
    void shouldThrowExceptionWhenValidatingUnavailableAccessory() {
        // given
        var configuration = new SmartphoneConfiguration("Black", 4000, List.of("Wireless Charger"));

        // when & then
        assertThatThrownBy(() -> smartphone.validateConfiguration(configuration))
                .isInstanceOf(InvalidProductConfigurationException.class)
                .hasMessageContaining("Accessory Wireless Charger is not available");
    }

    @Test
    void shouldThrowExceptionWhenConfigurationTypeDoesNotMatch() {
        // when & then
        assertThatThrownBy(() -> smartphone.validateConfiguration(new NoConfiguration()))
                .isInstanceOf(InvalidProductConfigurationException.class)
                .hasMessageContaining("Invalid configuration type for Smartphone");
    }

    @Test
    void shouldCreateCartItemWhenConfigurationIsValid() {
        // given
        var configuration = new SmartphoneConfiguration("Silver", 5000, List.of("Case", "Glass"));

        var cartItem = smartphone.toCartItem(configuration, 1);

        // then
        assertThat(cartItem.getProductId()).isEqualTo("S1");
        assertThat(cartItem.getConfiguration()).isEqualTo(configuration);
        assertThat(cartItem.getQuantity()).isEqualTo(1);
    }

    @Test
    void shouldNotCreateCartItemWhenConfigurationIsInvalid() {
        // given
        var configuration = new SmartphoneConfiguration("Gold", 5000, List.of());

        // when & then
        assertThatThrownBy(() -> smartphone.toCartItem(configuration, 1))
                .isInstanceOf(InvalidProductConfigurationException.class);
    }
}