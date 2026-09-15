package pl.adrian.electroshop.repository.inmemory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.adrian.electroshop.model.customer.Customer;
import pl.adrian.electroshop.model.invoice.Invoice;
import pl.adrian.electroshop.model.order.Order;
import pl.adrian.electroshop.model.product.CartItem;
import pl.adrian.electroshop.model.product.Electronics;
import pl.adrian.electroshop.model.product.configuration.NoConfiguration;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemoryInvoiceRepositoryTest {

    private InMemoryInvoiceRepository repository;
    private Invoice sampleInvoice;

    @BeforeEach
    void setUp() {
        repository = new InMemoryInvoiceRepository();

        Customer customer = new Customer("CU1", "Jan", "Kowalski", "jan.kowalski@test.pl");
        Electronics cable = new Electronics("E1", "USB-C Cable", new BigDecimal("49.99"), 100);
        CartItem cartItem = cable.toCartItem(new NoConfiguration(), 2);
        Order order = new Order("OR1", Instant.now(), customer, List.of(cartItem), BigDecimal.ZERO);

        sampleInvoice = new Invoice("FV/2026/07/1", LocalDate.of(2026, 7, 21), order);
    }

    @Test
    void shouldSaveAndFindInvoice() {
        // when
        repository.save(sampleInvoice);
        Optional<Invoice> found = repository.findByInvoiceNumber("FV/2026/07/1");

        // then
        assertThat(found).isPresent().contains(sampleInvoice);
    }

    @Test
    void shouldOverwriteInvoiceWhenSavingWithSameNumber() {
        // given
        repository.save(sampleInvoice);

        Customer customer = new Customer("CU2", "Anna", "Nowak", "anna.nowak@test.pl");
        Electronics cable = new Electronics("E2", "HDMI Cable", new BigDecimal("29.99"), 50);
        CartItem cartItem = cable.toCartItem(new NoConfiguration(), 1);
        Order updatedOrder = new Order("OR2", Instant.now(), customer, List.of(cartItem), BigDecimal.ZERO);
        Invoice updatedInvoice = new Invoice("FV/2026/07/1", LocalDate.of(2026, 7, 22), updatedOrder);

        // when
        repository.save(updatedInvoice);
        Optional<Invoice> found = repository.findByInvoiceNumber("FV/2026/07/1");

        // then
        assertThat(found).isPresent().contains(updatedInvoice);
        assertThat(found.get().getOrder().getOrderId()).isEqualTo("OR2");
    }

    @Test
    void shouldThrowExceptionWhenSavingNullInvoice() {
        // when & then
        assertThatThrownBy(() -> repository.save(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("is marked non-null but is null");
    }

    @Test
    void shouldReturnEmptyOptionalWhenInvoiceDoesNotExist() {
        // when
        Optional<Invoice> found = repository.findByInvoiceNumber("NON_EXISTENT");

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void shouldThrowExceptionWhenFindingWithNullInvoiceNumber() {
        // when & then
        assertThatThrownBy(() -> repository.findByInvoiceNumber(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("is marked non-null but is null");
    }

    @Test
    void shouldReturnAllSavedInvoices() {
        // given
        Customer customer = new Customer("CU2", "Anna", "Nowak", "anna.nowak@test.pl");
        Electronics cable = new Electronics("E2", "HDMI Cable", new BigDecimal("29.99"), 50);
        CartItem cartItem = cable.toCartItem(new NoConfiguration(), 1);
        Order secondOrder = new Order("OR2", Instant.now(), customer, List.of(cartItem), BigDecimal.ZERO);
        Invoice secondInvoice = new Invoice("FV/2026/07/2", LocalDate.of(2026, 7, 21), secondOrder);

        repository.save(sampleInvoice);
        repository.save(secondInvoice);

        // when
        List<Invoice> all = repository.findAll();

        // then
        assertThat(all).hasSize(2).contains(sampleInvoice, secondInvoice);
    }

    @Test
    void shouldReturnEmptyListWhenNoInvoicesSaved() {
        // when
        List<Invoice> all = repository.findAll();

        // then
        assertThat(all).isEmpty();
    }

    @Test
    void shouldReturnUnmodifiableListFromFindAll() {
        // given
        repository.save(sampleInvoice);
        List<Invoice> all = repository.findAll();

        // when & then
        assertThatThrownBy(() -> all.add(sampleInvoice))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}