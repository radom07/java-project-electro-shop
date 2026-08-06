package pl.adrian.electroshop.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.adrian.electroshop.exception.InsufficientStockException;
import pl.adrian.electroshop.exception.ProductNotFoundException;
import pl.adrian.electroshop.model.cart.Cart;
import pl.adrian.electroshop.model.customer.Customer;
import pl.adrian.electroshop.model.order.Order;
import pl.adrian.electroshop.model.product.Electronics;
import pl.adrian.electroshop.model.product.Product;
import pl.adrian.electroshop.model.product.configuration.NoConfiguration;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private ProductManager productManager;

    private Clock fixedClock;
    private Cart cart;
    private DiscountService discountService;
    private CartService cartService;

    private Product cable;
    private Customer customer;

    @BeforeEach
    void setUp() {
        cart = new Cart();
        fixedClock = Clock.fixed(Instant.parse("2026-08-04T10:00:00Z"), ZoneId.of("UTC"));
        discountService = new DiscountService();
        cartService = new CartService(productManager, cart, fixedClock, discountService);
        cable = new Electronics("E1", "USB-C Cable", new BigDecimal("49.99"), 10);
        customer = new Customer("CU1", "Jan", "Kowalski", "jan.kowalski@test.pl");
    }

    @Test
    void shouldAddProductToCartWhenStockIsSufficient() {
        // given
        when(productManager.getProduct("E1")).thenReturn(Optional.of(cable));

        // when
        cartService.addToCart("E1", new NoConfiguration(), 3);

        // then
        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(3);
    }

    @Test
    void shouldThrowExceptionWhenProductDoesNotExist() {
        // given
        when(productManager.getProduct("E1")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> cartService.addToCart("E1", new NoConfiguration(), 1))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    void shouldThrowExceptionWhenStockIsInsufficient() {
        // given
        when(productManager.getProduct("E1")).thenReturn(Optional.of(cable));

        // when & then
        assertThatThrownBy(() -> cartService.addToCart("E1", new NoConfiguration(), 20))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Not enough stock");

        // then
        assertThat(cart.isEmpty()).isTrue();
    }

    @Test
    void shouldAccountForQuantityAlreadyInCartWhenCheckingStock() {
        // given
        when(productManager.getProduct("E1")).thenReturn(Optional.of(cable));
        cartService.addToCart("E1", new NoConfiguration(), 7);

        // when & then
        assertThatThrownBy(() -> cartService.addToCart("E1", new NoConfiguration(), 5))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Not enough stock");
    }

    @Test
    void shouldReturnCartItemsWhenViewingCart() {
        // given
        when(productManager.getProduct("E1")).thenReturn(Optional.of(cable));
        cartService.addToCart("E1", new NoConfiguration(), 2);

        // when & then
        assertThat(cartService.viewCart()).hasSize(1);
    }

    @Test
    void shouldThrowExceptionWhenPlacingOrderWithNullCustomer() {
        // when & then
        assertThatThrownBy(() -> cartService.placeOrder(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Customer cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenPlacingOrderWithEmptyCart() {
        // when & then
        assertThatThrownBy(() -> cartService.placeOrder(customer))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cart is empty");
    }

    @Test
    void shouldCreateOrderAndReserveStockAndClearCartWhenPlacingOrder() {
        // given
        when(productManager.getProduct("E1")).thenReturn(Optional.of(cable));
        cartService.addToCart("E1", new NoConfiguration(), 3);

        // when
        Order order = cartService.placeOrder(customer);

        // then
        assertThat(order).isNotNull();
        assertThat(order.getOrderId()).isNotBlank();
        assertThat(order.getCustomerId()).isEqualTo("CU1");
        assertThat(order.getOrderedItems()).hasSize(1);
        assertThat(order.getSubtotal()).isEqualByComparingTo("149.97");
        assertThat(order.getDiscountAmount()).isEqualByComparingTo("0.00");
        assertThat(order.getTotalAmount()).isEqualByComparingTo("149.97");

        verify(productManager, times(1)).reserveStock("E1", 3);
        assertThat(cart.isEmpty()).isTrue();
    }

    @Test
    void shouldGenerateDifferentOrderIdsForConsecutiveOrdersFromSameCart() {
        // given
        when(productManager.getProduct("E1")).thenReturn(Optional.of(cable));
        cartService.addToCart("E1", new NoConfiguration(), 1);
        Order firstOrder = cartService.placeOrder(customer);

        cartService.addToCart("E1", new NoConfiguration(), 1);

        // when
        Order secondOrder = cartService.placeOrder(customer);

        // then
        assertThat(firstOrder.getOrderId()).isNotEqualTo(secondOrder.getOrderId());
    }

    @Test
    void shouldThrowExceptionWhenPlacingOrderAndStockBecameInsufficient() {
        // given
        when(productManager.getProduct("E1")).thenReturn(Optional.of(cable));
        cartService.addToCart("E1", new NoConfiguration(), 3);

        doThrow(new InsufficientStockException("E1"))
                .when(productManager).reserveStock("E1", 3);

        // when & then
        assertThatThrownBy(() -> cartService.placeOrder(customer))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Not enough stock");

        // then
        assertThat(cart.isEmpty()).isFalse();
    }

    @Test
    void shouldThrowExceptionWhenPlacingOrderAndProductWasRemoved() {
        // given
        when(productManager.getProduct("E1")).thenReturn(Optional.of(cable));
        cartService.addToCart("E1", new NoConfiguration(), 2);

        // Symulacja: produkt usunięty z magazynu między dodaniem do koszyka a złożeniem zamówienia
        doThrow(new ProductNotFoundException("E1"))
                .when(productManager).reserveStock("E1", 2);

        // when & then
        assertThatThrownBy(() -> cartService.placeOrder(customer))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    void shouldReleaseAlreadyReservedItemsWhenLaterItemFailsToReserve() {
        // given
        Product mouse = new Electronics("E2", "Wireless Mouse", new BigDecimal("29.99"), 10);
        when(productManager.getProduct("E1")).thenReturn(Optional.of(cable));
        when(productManager.getProduct("E2")).thenReturn(Optional.of(mouse));

        cartService.addToCart("E1", new NoConfiguration(), 2); // ta rezerwacja się powiedzie
        cartService.addToCart("E2", new NoConfiguration(), 1); // ta rzuci wyjątek

        doNothing().when(productManager).reserveStock("E1", 2);
        doThrow(new InsufficientStockException("E2"))
                .when(productManager).reserveStock("E2", 1);

        // when & then
        assertThatThrownBy(() -> cartService.placeOrder(customer))
                .isInstanceOf(InsufficientStockException.class);

        // then
        verify(productManager, times(1)).reserveStock("E1", 2);
        verify(productManager, times(1)).releaseStock("E1", 2);
    }

    @Test
    void shouldCalculateDiscountWhenValidDiscountCodeProvided() {
        // given
        when(productManager.getProduct("E1")).thenReturn(Optional.of(cable));
        cartService.addToCart("E1", new NoConfiguration(), 2); // 49.99 * 2 = 99.98

        // when
        Order order = cartService.placeOrder(customer, "WELCOME10");

        // then
        assertThat(order.getSubtotal()).isEqualByComparingTo("99.98");
        assertThat(order.getDiscountAmount()).isEqualByComparingTo("9.998");
        assertThat(order.getTotalAmount()).isEqualByComparingTo("89.982");
    }

    @Test
    void shouldApplyZeroDiscountWhenInvalidDiscountCodeProvided() {
        // given
        when(productManager.getProduct("E1")).thenReturn(Optional.of(cable));
        cartService.addToCart("E1", new NoConfiguration(), 2);

        // when
        Order order = cartService.placeOrder(customer, "FAKECODE");

        // then
        assertThat(order.getSubtotal()).isEqualByComparingTo("99.98");
        assertThat(order.getDiscountAmount()).isEqualByComparingTo("0.00");
        assertThat(order.getTotalAmount()).isEqualByComparingTo("99.98");
    }
}