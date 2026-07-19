package pl.adrian.electroshop.model.order;

import org.junit.jupiter.api.Test;
import pl.adrian.electroshop.model.customer.Customer;
import pl.adrian.electroshop.model.product.CartItem;
import pl.adrian.electroshop.model.product.Electronics;
import pl.adrian.electroshop.model.product.Product;
import pl.adrian.electroshop.model.product.configuration.NoConfiguration;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static java.beans.Beans.isInstanceOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    private Customer createTestCustomer() {
        return new Customer("1234", "Adrian", "Kowalski", "adrian@test.pl");
    }

    @Test
    void shouldCreateOrderWhenDataIsValid() {
        // given
        Customer testCustomer = createTestCustomer();
        Product testProduct = new Electronics("000", "Keyboard", new BigDecimal("299.99"), 10);
        CartItem item = testProduct.toCartItem(new NoConfiguration(), 1);
        List<CartItem> items = List.of(item);

        // when
        Order order = new Order("1", testCustomer, items, new BigDecimal("299.99"));

        // then
        assertThat(order.getOrderId()).isEqualTo("1");
        assertThat(order.getOrderedItems()).hasSize(1);
        assertThat(order.getTotalAmount()).isEqualTo(new BigDecimal("299.99"));
    }

    @Test
    void shouldThrowExceptionWhenCartIsEmpty() {
        // given
        Customer testCustomer = createTestCustomer();
        List<CartItem> emptyItems = List.of();

        // when & then
        assertThatThrownBy(() -> new Order("1", testCustomer, emptyItems, new BigDecimal("0.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot create order with empty cart.");
    }

    @Test
    void shouldThrowExceptionWhenCustomerIsNull() {
        // given
        Product testProduct = new Electronics("000", "Keyboard", new BigDecimal("299.99"), 10);
        CartItem item = testProduct.toCartItem(new NoConfiguration(), 1);

        // when & then
        assertThatThrownBy(() -> new Order("1", null, List.of(item), new BigDecimal("299.99")))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldMakeImmutableCopyOfOrderedItems() {
        // given
        Customer testCustomer = createTestCustomer();
        Product testProduct = new Electronics("000", "Keyboard", new BigDecimal("299.99"), 10);
        CartItem item = testProduct.toCartItem(new NoConfiguration(), 1);

        Product productToAdd = new Electronics("001", "TV", new BigDecimal("1299.99"), 5);
        CartItem itemToAdd = testProduct.toCartItem(new NoConfiguration(), 1);

        List<CartItem> modifiableList = new ArrayList<>();
        modifiableList.add(item);

        Order order = new Order("1", testCustomer, modifiableList, new BigDecimal("299.99"));

        // when & then
        assertThatThrownBy(() -> order.getOrderedItems().add(itemToAdd))
                .isInstanceOf(UnsupportedOperationException.class);

        modifiableList.add(itemToAdd);
        assertThat(order.getOrderedItems()).hasSize(1);
    }
}