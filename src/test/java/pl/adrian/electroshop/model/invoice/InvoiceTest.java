package pl.adrian.electroshop.model.invoice;

import org.junit.jupiter.api.Test;
import pl.adrian.electroshop.model.customer.Customer;
import pl.adrian.electroshop.model.order.Order;
import pl.adrian.electroshop.model.product.CartItem;
import pl.adrian.electroshop.model.product.Electronics;
import pl.adrian.electroshop.model.product.configuration.NoConfiguration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InvoiceTest {

    private Order createTestOrder() {
        Customer customer = new Customer("CU1", "Jan", "Kowalski", "jan.kowalski@test.pl");
        Electronics cable = new Electronics("E1", "USB-C Cable", new BigDecimal("49.99"), 100);
        CartItem cartItem = cable.toCartItem(new NoConfiguration(), 2);
        return new Order("OR1", LocalDateTime.now(), customer, List.of(cartItem), new BigDecimal("99.98"));
    }

    @Test
    void shouldCreateInvoiceWithValidData() {
        // given
        Order order = createTestOrder();
        LocalDate issueDate = LocalDate.of(2026, 7, 24);

        // when
        Invoice invoice = new Invoice("FV/2026/07/1", issueDate, order);

        // then
        assertThat(invoice.getInvoiceNumber()).isEqualTo("FV/2026/07/1");
        assertThat(invoice.getIssueDate()).isEqualTo(issueDate);
        assertThat(invoice.getOrder()).isEqualTo(order);
    }

    @Test
    void shouldThrowExceptionWhenInvoiceNumberIsNull() {
        // given
        Order order = createTestOrder();

        // when & then
        assertThatThrownBy(() -> new Invoice(null, LocalDate.now(), order))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldThrowExceptionWhenIssueDateIsNull() {
        // given
        Order order = createTestOrder();

        // when & then
        assertThatThrownBy(() -> new Invoice("FV/2026/07/1", null, order))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldThrowExceptionWhenOrderIsNull() {
        // when & then
        assertThatThrownBy(() -> new Invoice("FV/2026/07/1", LocalDate.now(), null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void toStringShouldContainKeyInvoiceData() {
        // given
        Order order = createTestOrder();
        Invoice invoice = new Invoice("FV/2026/07/1", LocalDate.of(2026, 7, 24), order);

        // when
        String result = invoice.toString();

        // then
        assertThat(result)
                .contains("FV/2026/07/1")
                .contains("2026-07-24")
                .contains("OR1")
                .contains("99,98");
    }
}