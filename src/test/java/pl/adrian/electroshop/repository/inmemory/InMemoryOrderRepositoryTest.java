package pl.adrian.electroshop.repository.inmemory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.adrian.electroshop.model.customer.Customer;
import pl.adrian.electroshop.model.order.Order;
import pl.adrian.electroshop.model.product.CartItem;
import pl.adrian.electroshop.model.product.Electronics;
import pl.adrian.electroshop.model.product.configuration.NoConfiguration;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemoryOrderRepositoryTest {

    private InMemoryOrderRepository repository;
    private Customer sampleCustomer;
    private Order sampleOrder;

    @BeforeEach
    void setUp() {
        repository = new InMemoryOrderRepository();
        sampleCustomer = new Customer("CU1", "Jan", "Kowalski", "jan.kowalski@test.pl");

        Electronics cable = new Electronics("E1", "USB-C Cable", new BigDecimal("49.99"), 100);
        CartItem cartItem = cable.toCartItem(new NoConfiguration(), 2);

        sampleOrder = new Order("OR1", sampleCustomer, List.of(cartItem), new BigDecimal("99.98"));
    }

    @Test
    void shouldSaveAndFindOrder() {
        // when
        repository.save(sampleOrder);
        Optional<Order> found = repository.findById("OR1");

        // then
        assertThat(found).isPresent().contains(sampleOrder);
    }

    @Test
    void shouldOverwriteOrderWhenSavingWithSameId() {
        // given
        repository.save(sampleOrder);

        Electronics cable = new Electronics("E2", "HDMI Cable", new BigDecimal("29.99"), 50);
        CartItem otherItem = cable.toCartItem(new NoConfiguration(), 1);
        Order updatedOrder = new Order("OR1", sampleCustomer, List.of(otherItem), new BigDecimal("29.99"));

        // when
        repository.save(updatedOrder);
        Optional<Order> found = repository.findById("OR1");

        // then
        assertThat(found).isPresent().contains(updatedOrder);
        assertThat(found.get().getTotalAmount()).isEqualByComparingTo("29.99");
    }

    @Test
    void shouldThrowExceptionWhenSavingNullOrder() {
        // when & then
        assertThatThrownBy(() -> repository.save(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Order cannot be null");
    }

    @Test
    void shouldDeleteOrderById() {
        // given
        repository.save(sampleOrder);

        // when
        repository.deleteById("OR1");
        Optional<Order> found = repository.findById("OR1");

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void shouldThrowExceptionWhenDeletingWithNullId() {
        // when & then
        assertThatThrownBy(() -> repository.deleteById(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID cannot be null");
    }

    @Test
    void shouldReturnEmptyOptionalWhenOrderDoesNotExist() {
        // when
        Optional<Order> found = repository.findById("NON_EXISTENT");

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void shouldThrowExceptionWhenFindingWithNullId() {
        // when & then
        assertThatThrownBy(() -> repository.findById(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID cannot be null");
    }

    @Test
    void shouldReturnAllSavedOrders() {
        // given
        Electronics cable = new Electronics("E2", "HDMI Cable", new BigDecimal("29.99"), 50);
        CartItem otherItem = cable.toCartItem(new NoConfiguration(), 1);
        Order secondOrder = new Order("OR2", sampleCustomer, List.of(otherItem), new BigDecimal("29.99"));

        repository.save(sampleOrder);
        repository.save(secondOrder);

        // when
        List<Order> all = repository.findAll();

        // then
        assertThat(all).hasSize(2).contains(sampleOrder, secondOrder);
    }

    @Test
    void shouldReturnEmptyListWhenNoOrdersSaved() {
        // when
        List<Order> all = repository.findAll();

        // then
        assertThat(all).isEmpty();
    }

    @Test
    void shouldReturnUnmodifiableListFromFindAll() {
        // given
        repository.save(sampleOrder);
        List<Order> all = repository.findAll();

        // when & then
        assertThatThrownBy(() -> all.add(sampleOrder))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}