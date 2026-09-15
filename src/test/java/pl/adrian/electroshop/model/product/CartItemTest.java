package pl.adrian.electroshop.model.product;

import org.junit.jupiter.api.Test;
import pl.adrian.electroshop.model.product.configuration.NoConfiguration;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CartItemTest {

    private final Electronics electronics =
            new Electronics("E1", "USB-C Cable", new BigDecimal("49.99"), 100);

    @Test
    void shouldCalculateSubtotalCorrectly() {
        // given
        var cartItem = electronics.toCartItem(new NoConfiguration(), 3);

        // when & then
        assertThat(cartItem.getSubtotal()).isEqualByComparingTo("149.97");
    }

    @Test
    void shouldUpdateQuantity() {
        // given
        var cartItem = electronics.toCartItem(new NoConfiguration(), 1);

        cartItem.setQuantity(5);

        // then
        assertThat(cartItem.getQuantity()).isEqualTo(5);
        assertThat(cartItem.getSubtotal()).isEqualByComparingTo("249.95");
    }

    @Test
    void shouldThrowExceptionWhenCreatingWithZeroQuantity() {
        // when & then
        assertThatThrownBy(() -> electronics.toCartItem(new NoConfiguration(), 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantity must be greater than 0");
    }

    @Test
    void shouldThrowExceptionWhenCreatingWithNegativeQuantity() {
        // when & then
        assertThatThrownBy(() -> electronics.toCartItem(new NoConfiguration(), -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantity must be greater than 0");
    }

    @Test
    void shouldThrowExceptionWhenUpdatingToZeroOrNegativeQuantity() {
        // given
        var cartItem = electronics.toCartItem(new NoConfiguration(), 1);

        // when & then
        assertThatThrownBy(() -> cartItem.setQuantity(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> cartItem.setQuantity(-5))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void priceShouldRemainFrozenAfterProductPriceChanges() {
        // given
        var cartItem = electronics.toCartItem(new NoConfiguration(), 1);

        electronics.setPrice(new BigDecimal("999.00"));

        // then
        assertThat(cartItem.getUnitPrice()).isEqualByComparingTo("49.99");
    }
}