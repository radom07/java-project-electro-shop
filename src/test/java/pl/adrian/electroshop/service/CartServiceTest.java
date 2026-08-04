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
        when(productManager.getProduct("E1")).thenReturn(Optional.of(cable));

        cartService.addToCart("E1", new NoConfiguration(), 3);

        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(3);
    }

    @Test
    void shouldThrowExceptionWhenProductDoesNotExist() {
        when(productManager.getProduct("E1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addToCart("E1", new NoConfiguration(), 1))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    void shouldThrowExceptionWhenStockIsInsufficient() {
        when(productManager.getProduct("E1")).thenReturn(Optional.of(cable));

        assertThatThrownBy(() -> cartService.addToCart("E1", new NoConfiguration(), 20))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Not enough stock");

        assertThat(cart.isEmpty()).isTrue();
    }

    @Test
    void shouldAccountForQuantityAlreadyInCartWhenCheckingStock() {
        when(productManager.getProduct("E1")).thenReturn(Optional.of(cable));

        cartService.addToCart("E1", new NoConfiguration(), 7);

        assertThatThrownBy(() -> cartService.addToCart("E1", new NoConfiguration(), 5))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Not enough stock");
    }

    @Test
    void shouldReturnCartItemsWhenViewingCart() {
        when(productManager.getProduct("E1")).thenReturn(Optional.of(cable));
        cartService.addToCart("E1", new NoConfiguration(), 2);

        assertThat(cartService.viewCart()).hasSize(1);
    }

    @Test
    void shouldThrowExceptionWhenPlacingOrderWithNullCustomer() {
        assertThatThrownBy(() -> cartService.placeOrder(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Customer cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenPlacingOrderWithEmptyCart() {
        assertThatThrownBy(() -> cartService.placeOrder(customer))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cart is empty");
    }

    @Test
    void shouldCreateOrderAndReduceStockAndClearCartWhenPlacingOrder() {
        when(productManager.getProduct("E1")).thenReturn(Optional.of(cable));
        cartService.addToCart("E1", new NoConfiguration(), 3);

        Order order = cartService.placeOrder(customer);

        assertThat(order).isNotNull();
        assertThat(order.getOrderId()).isNotBlank();
        assertThat(order.getCustomerId()).isEqualTo("CU1");
        assertThat(order.getOrderedItems()).hasSize(1);
        assertThat(order.getSubtotal()).isEqualByComparingTo("149.97");
        assertThat(order.getDiscountAmount()).isEqualByComparingTo("0.00");
        assertThat(order.getTotalAmount()).isEqualByComparingTo("149.97"); // 49.99 * 3

        assertThat(cable.getQuantity()).isEqualTo(7); // 10 - 3
        verify(productManager, times(1)).updateProduct(cable);
        assertThat(cart.isEmpty()).isTrue();
    }

    @Test
    void shouldGenerateDifferentOrderIdsForConsecutiveOrdersFromSameCart() {
        when(productManager.getProduct("E1")).thenReturn(Optional.of(cable));

        cartService.addToCart("E1", new NoConfiguration(), 1);
        Order firstOrder = cartService.placeOrder(customer);

        cable.setQuantity(cable.getQuantity() + 5); // uzupełnienie stanu magazynowego
        cartService.addToCart("E1", new NoConfiguration(), 1);
        Order secondOrder = cartService.placeOrder(customer);

        assertThat(firstOrder.getOrderId()).isNotEqualTo(secondOrder.getOrderId());
    }

    @Test
    void shouldThrowExceptionWhenPlacingOrderAndStockBecameInsufficient() {
        when(productManager.getProduct("E1")).thenReturn(Optional.of(cable));
        cartService.addToCart("E1", new NoConfiguration(), 3);

        // Symulacja: stan magazynowy spadł po dodaniu do koszyka, przed złożeniem zamówienia
        cable.setQuantity(1);

        assertThatThrownBy(() -> cartService.placeOrder(customer))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Not enough stock");

        verify(productManager, never()).updateProduct(any());
        assertThat(cart.isEmpty()).isFalse(); // koszyk nie czyszczony przy nieudanej próbie
    }

    @Test
    void shouldThrowExceptionWhenPlacingOrderAndProductWasRemoved() {
        when(productManager.getProduct("E1"))
                .thenReturn(Optional.of(cable))  // podczas addToCart
                .thenReturn(Optional.empty());   // podczas placeOrder - produkt usunięty z magazynu

        cartService.addToCart("E1", new NoConfiguration(), 2);

        assertThatThrownBy(() -> cartService.placeOrder(customer))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    void shouldCalculateDiscountWhenValidDiscountCodeProvided() {
        when(productManager.getProduct("E1")).thenReturn(Optional.of(cable));
        cartService.addToCart("E1", new NoConfiguration(), 2); // 49.99 * 2 = 99.98

        Order order = cartService.placeOrder(customer, "WELCOME10");

        // 10% rabatu od 100 zł
        assertThat(order.getSubtotal()).isEqualByComparingTo("99.98");
        assertThat(order.getDiscountAmount()).isEqualByComparingTo("9.998");
        assertThat(order.getTotalAmount()).isEqualByComparingTo("89.982");
    }

    @Test
    void shouldApplyZeroDiscountWhenInvalidDiscountCodeProvided() {
        when(productManager.getProduct("E1")).thenReturn(Optional.of(cable));
        cartService.addToCart("E1", new NoConfiguration(), 2);

        Order order = cartService.placeOrder(customer, "FAKECODE");

        // Brak rabatu
        assertThat(order.getSubtotal()).isEqualByComparingTo("99.98");
        assertThat(order.getDiscountAmount()).isEqualByComparingTo("0.00");
        assertThat(order.getTotalAmount()).isEqualByComparingTo("99.98");
    }
}