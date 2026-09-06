package pl.adrian.electroshop.model.order;

import org.junit.jupiter.api.Test;
import pl.adrian.electroshop.exception.InvalidOrderStatusTransitionException;
import pl.adrian.electroshop.model.customer.Customer;
import pl.adrian.electroshop.model.product.CartItem;
import pl.adrian.electroshop.model.product.Electronics;
import pl.adrian.electroshop.model.product.Product;
import pl.adrian.electroshop.model.product.configuration.NoConfiguration;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        Order order = new Order("1", Instant.now(), testCustomer, items, BigDecimal.ZERO);

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
        assertThatThrownBy(() -> new Order("1", Instant.now(), testCustomer, emptyItems, BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot create order with empty cart.");
    }

    @Test
    void shouldThrowExceptionWhenCustomerIsNull() {
        // given
        Product testProduct = new Electronics("000", "Keyboard", new BigDecimal("299.99"), 10);
        CartItem item = testProduct.toCartItem(new NoConfiguration(), 1);

        // when & then
        assertThatThrownBy(() -> new Order("1", Instant.now(), null, List.of(item), BigDecimal.ZERO))
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
        OrderLine orderLine = new OrderLine(itemToAdd);

        List<CartItem> modifiableList = new ArrayList<>();
        modifiableList.add(item);

        Order order = new Order("1", Instant.now(), testCustomer, modifiableList, BigDecimal.ZERO);

        // when & then
        assertThatThrownBy(() -> order.getOrderedItems().add(orderLine))
                .isInstanceOf(UnsupportedOperationException.class);

        modifiableList.add(itemToAdd);
        assertThat(order.getOrderedItems()).hasSize(1);
    }

    @Test
    void shouldNotChangeOrderDetailWhenChangingCustomerData() {
        // given
        Customer testCustomer = createTestCustomer();
        Product testProduct = new Electronics("000", "Keyboard", new BigDecimal("299.99"), 10);
        CartItem item = testProduct.toCartItem(new NoConfiguration(), 1);
        List<CartItem> items = List.of(item);
        Order order = new Order("1", Instant.now(), testCustomer, items, BigDecimal.ZERO);

        // when
        testCustomer.setFirstName("Tomasz");

        // then
        assertThat(testCustomer.getFirstName()).isEqualTo("Tomasz");
        assertThat(order.getCustomerFirstName()).isEqualTo("Adrian");
    }

    @Test
    void shouldPlacedAtExactly() {
        // given
        Customer testCustomer = createTestCustomer();
        Product testProduct = new Electronics("000", "Keyboard", new BigDecimal("299.99"), 10);
        CartItem item = testProduct.toCartItem(new NoConfiguration(), 1);
        Instant placedAt = Instant.parse("2026-07-29T23:34:00Z");

        // when
        Order order = new Order("1", placedAt, testCustomer, List.of(item), BigDecimal.ZERO);

        // then
        assertThat(order.getPlacedAt()).isEqualTo(placedAt);
    }

    @Test
    void shouldThrowExceptionWhenPlacedAtIsNull() {
        // given
        Customer testCustomer = createTestCustomer();
        Product testProduct = new Electronics("000", "Keyboard", new BigDecimal("299.99"), 10);
        CartItem item = testProduct.toCartItem(new NoConfiguration(), 1);

        // when & then
        assertThatThrownBy(() -> new Order("1", null, testCustomer, List.of(item), BigDecimal.ZERO))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldChangeStatusWhenTransitionIsValid() {
        // given
        Order order = new Order("1", Instant.now(), createTestCustomer(),
                List.of(new Electronics("000", "Keyboard", new BigDecimal("299.99"), 10)
                        .toCartItem(new NoConfiguration(), 1)), BigDecimal.ZERO);

        // when
        order.changeStatus(OrderStatus.PAID);

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    void shouldThrowExceptionWhenTransitionIsInvalid() {
        // given
        Order order = new Order("1", Instant.now(), createTestCustomer(),
                List.of(new Electronics("000", "Keyboard", new BigDecimal("299.99"), 10)
                        .toCartItem(new NoConfiguration(), 1)), BigDecimal.ZERO);

        // when & then
        assertThatThrownBy(() -> order.changeStatus(OrderStatus.SHIPPED))
                .isInstanceOf(InvalidOrderStatusTransitionException.class)
                .hasMessageContaining("Cannot change order status");
    }

    @Test
    void shouldCalculateTotalAmountWhenDiscountIsApplied() {
        // given
        Customer testCustomer = createTestCustomer();
        Product testProduct = new Electronics("000", "Keyboard", new BigDecimal("1000.00"), 10);
        CartItem item = testProduct.toCartItem(new NoConfiguration(), 1);
        List<CartItem> items = List.of(item);

        // when
        Order order = new Order("1", Instant.now(), testCustomer, items, new BigDecimal("100.00"));

        // then
        assertThat(order.getTotalAmount()).isEqualTo(new BigDecimal("900.00"));
    }

    @Test
    void shouldThrowExceptionWhenDiscountIsGreaterThanSubtotal() {
        // when & then
        assertThatThrownBy(() -> new Order("1", Instant.now(), createTestCustomer(),
                List.of(new Electronics("000", "Keyboard", new BigDecimal("299.99"), 10)
                        .toCartItem(new NoConfiguration(), 1)), new BigDecimal("300.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Discount amount cannot exceed subtotal.");
    }
}