package pl.adrian.electroshop.model.cart;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.adrian.electroshop.model.product.CartItem;
import pl.adrian.electroshop.model.product.Computer;
import pl.adrian.electroshop.model.product.Electronics;
import pl.adrian.electroshop.model.product.configuration.ComputerConfiguration;
import pl.adrian.electroshop.model.product.configuration.NoConfiguration;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CartTest {

    private Cart cart;
    private Electronics cable;
    private Computer computer;

    @BeforeEach
    void setUp() {
        // given
        cart = new Cart();
        cable = new Electronics("E1", "USB-C Cable", new BigDecimal("49.99"), 100);
        computer = new Computer("C1", "Dell XPS", new BigDecimal("4999.99"), 10,
                List.of("Intel i5", "Intel i7"), List.of(8, 16));
    }

    @Test
    void shouldBeEmptyWhenCreated() {
        // then
        assertThat(cart.isEmpty()).isTrue();
        assertThat(cart.getItemCount()).isZero();
        assertThat(cart.getTotal()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldAddNewItemToCart() {
        // given
        CartItem item = cable.toCartItem(new NoConfiguration(), 2);

        cart.addItem(item);

        // then
        assertThat(cart.isEmpty()).isFalse();
        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItemCount()).isEqualTo(1);
    }

    @Test
    void shouldMergeQuantitiesWhenAddingSameProductWithSameConfiguration() {
        // given
        cart.addItem(cable.toCartItem(new NoConfiguration(), 2));
        cart.addItem(cable.toCartItem(new NoConfiguration(), 3));

        // then
        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(5);
    }

    @Test
    void shouldTreatSameProductWithDifferentConfigurationAsSeparateItems() {
        // given
        cart.addItem(computer.toCartItem(new ComputerConfiguration("Intel i5", 8), 1));
        cart.addItem(computer.toCartItem(new ComputerConfiguration("Intel i7", 16), 1));

        // then
        assertThat(cart.getItems()).hasSize(2);
        assertThat(cart.getItemCount()).isEqualTo(2);
    }

    @Test
    void shouldThrowExceptionWhenAddingNullItem() {
        // when & then
        assertThatThrownBy(() -> cart.addItem(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CartItem cannot be null");
    }

    @Test
    void shouldRemoveItemByProductIdAndConfiguration() {
        // given
        cart.addItem(cable.toCartItem(new NoConfiguration(), 2));

        cart.removeItem("E1", new NoConfiguration());

        // then
        assertThat(cart.isEmpty()).isTrue();
    }

    @Test
    void shouldRemoveAllItemsForProductRegardlessOfConfiguration() {
        // given
        cart.addItem(computer.toCartItem(new ComputerConfiguration("Intel i5", 8), 1));
        cart.addItem(computer.toCartItem(new ComputerConfiguration("Intel i7", 16), 1));

        cart.removeAllForProduct("C1");

        // then
        assertThat(cart.isEmpty()).isTrue();
    }

    @Test
    void shouldUpdateQuantityOfExistingItem() {
        // given
        cart.addItem(cable.toCartItem(new NoConfiguration(), 2));

        cart.updateQuantity("E1", new NoConfiguration(), 10);

        // then
        assertThat(cart.findItem("E1", new NoConfiguration()))
                .isPresent()
                .get()
                .extracting(CartItem::getQuantity)
                .isEqualTo(10);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingQuantityOfMissingItem() {
        // when & then
        assertThatThrownBy(() -> cart.updateQuantity("E1", new NoConfiguration(), 5))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Item not found in cart");
    }

    @Test
    void shouldCalculateTotalAcrossMultipleItems() {
        // given
        cart.addItem(cable.toCartItem(new NoConfiguration(), 2)); // 99.98
        cart.addItem(computer.toCartItem(new ComputerConfiguration("Intel i5", 8), 1)); // 4999.99

        // then
        assertThat(cart.getTotal()).isEqualByComparingTo("5099.97");
    }

    @Test
    void shouldClearAllItems() {
        // given
        cart.addItem(cable.toCartItem(new NoConfiguration(), 2));

        cart.clear();

        // then
        assertThat(cart.isEmpty()).isTrue();
        assertThat(cart.getTotal()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldReturnUnmodifiableListFromGetItems() {
        // given
        cart.addItem(cable.toCartItem(new NoConfiguration(), 1));

        List<CartItem> items = cart.getItems();

        // when & then
        assertThatThrownBy(() -> items.add(cable.toCartItem(new NoConfiguration(), 1)))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldHaveNonBlankCartId() {
        // then
        assertThat(cart.getCartId()).isNotBlank();
    }

    @Test
    void eachCartShouldHaveUniqueId() {
        Cart anotherCart = new Cart();

        // then
        assertThat(cart.getCartId()).isNotEqualTo(anotherCart.getCartId());
    }
}