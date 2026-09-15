package pl.adrian.electroshop.model.order;

import org.junit.jupiter.api.Test;
import pl.adrian.electroshop.model.product.CartItem;
import pl.adrian.electroshop.model.product.Electronics;
import pl.adrian.electroshop.model.product.configuration.NoConfiguration;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class OrderLineTest {

    @Test
    void shouldCopyDataFromCartItemAtCreationTime() {
        Electronics cable = new Electronics("E1", "USB-C Cable", new BigDecimal("49.99"), 100);
        CartItem cartItem = cable.toCartItem(new NoConfiguration(), 3);

        OrderLine orderLine = new OrderLine(cartItem);

        assertThat(orderLine.getProductId()).isEqualTo("E1");
        assertThat(orderLine.getProductName()).isEqualTo("USB-C Cable");
        assertThat(orderLine.getUnitPrice()).isEqualByComparingTo("49.99");
        assertThat(orderLine.getQuantity()).isEqualTo(3);
        assertThat(orderLine.getSubtotal()).isEqualByComparingTo("149.97");
    }

    @Test
    void shouldNotReflectLaterChangesToOriginalCartItem() {
        Electronics cable = new Electronics("E1", "USB-C Cable", new BigDecimal("49.99"), 100);
        CartItem cartItem = cable.toCartItem(new NoConfiguration(), 3);

        OrderLine orderLine = new OrderLine(cartItem);

        cartItem.setQuantity(999); //

        assertThat(orderLine.getQuantity()).isEqualTo(3);
    }
}