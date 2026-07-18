package pl.adrian.electroshop.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.adrian.electroshop.model.cart.Cart;
import pl.adrian.electroshop.model.product.Electronics;
import pl.adrian.electroshop.model.product.Product;
import pl.adrian.electroshop.model.product.configuration.NoConfiguration;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private ProductManager productManager;

    private Cart cart;
    private CartService cartService;

    private Product cable;

    @BeforeEach
    void setUp() {
        // given
        cart = new Cart();
        cartService = new CartService(productManager, cart);
        cable = new Electronics("E1", "USB-C Cable", new BigDecimal("49.99"), 10);
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
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    void shouldThrowExceptionWhenStockIsInsufficient() {
        // given
        when(productManager.getProduct("E1")).thenReturn(Optional.of(cable));

        // when & then
        assertThatThrownBy(() -> cartService.addToCart("E1", new NoConfiguration(), 20))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough stock");

        assertThat(cart.isEmpty()).isTrue();
    }

    @Test
    void shouldAccountForQuantityAlreadyInCartWhenCheckingStock() {
        // given
        when(productManager.getProduct("E1")).thenReturn(Optional.of(cable));

        // when
        cartService.addToCart("E1", new NoConfiguration(), 7);

        // then
        assertThatThrownBy(() -> cartService.addToCart("E1", new NoConfiguration(), 5))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough stock");
    }

    @Test
    void shouldReturnCartItemsWhenViewingCart() {
        // given
        when(productManager.getProduct("E1")).thenReturn(Optional.of(cable));

        // when
        cartService.addToCart("E1", new NoConfiguration(), 2);

        // then
        assertThat(cartService.viewCart()).hasSize(1);
    }

    @Test
    void shouldThrowExceptionWhenPlacingOrderWithEmptyCart() {
        // when && then
        assertThatThrownBy(() -> cartService.placeOrder())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cart is empty");
    }

    @Test
    void shouldReduceStockAndClearCartWhenPlacingOrder() {
        // given
        when(productManager.getProduct("E1")).thenReturn(Optional.of(cable));
        cartService.addToCart("E1", new NoConfiguration(), 3);

        // when
        cartService.placeOrder();

        // then
        assertThat(cable.getQuantity()).isEqualTo(7); // 10 - 3
        verify(productManager, times(1)).updateProduct(cable);
        assertThat(cart.isEmpty()).isTrue();
    }

    @Test
    void shouldThrowExceptionWhenPlacingOrderAndStockBecameInsufficient() {
        // given
        when(productManager.getProduct("E1")).thenReturn(Optional.of(cable));
        cartService.addToCart("E1", new NoConfiguration(), 3);

        // Symulacja: stan magazynowy spadł po dodaniu do koszyka, przed złożeniem zamówienia
        // when
        cable.setQuantity(1);

        // then
        assertThatThrownBy(() -> cartService.placeOrder())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough stock");

        verify(productManager, never()).updateProduct(any());
        assertThat(cart.isEmpty()).isFalse(); // koszyk nie czyszczony przy nieudanej próbie
    }

    @Test
    void shouldThrowExceptionWhenPlacingOrderAndProductWasRemoved() {
        // given
        when(productManager.getProduct("E1"))
                .thenReturn(Optional.of(cable))  // podczas addToCart
                .thenReturn(Optional.empty());   // podczas placeOrder - produkt usunięty z magazynu

        cartService.addToCart("E1", new NoConfiguration(), 2);

        // when & then
        assertThatThrownBy(() -> cartService.placeOrder())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Product no longer available");
    }
}