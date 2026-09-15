package pl.adrian.electroshop.repository.file.serialization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.adrian.electroshop.exception.CorruptedFileDataException;
import pl.adrian.electroshop.exception.OrderNotFoundException;
import pl.adrian.electroshop.model.customer.Customer;
import pl.adrian.electroshop.model.invoice.Invoice;
import pl.adrian.electroshop.model.order.Order;
import pl.adrian.electroshop.model.product.CartItem;
import pl.adrian.electroshop.model.product.Electronics;
import pl.adrian.electroshop.model.product.configuration.NoConfiguration;
import pl.adrian.electroshop.repository.OrderRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceFileSerializerTest {

    @Mock
    private OrderRepository orderRepository;

    private InvoiceFileSerializer serializer;
    private Invoice sampleInvoice;
    private Order sampleOrder;

    @BeforeEach
    void setUp() {
        serializer = new InvoiceFileSerializer(orderRepository);

        Customer customer = new Customer("CU1", "Jan", "Kowalski", "jan.kowalski@test.pl");
        Electronics cable = new Electronics("E1", "USB-C Cable", new BigDecimal("49.99"), 100);
        CartItem cartItem = cable.toCartItem(new NoConfiguration(), 2);

        sampleOrder = new Order("OR1", Instant.now(), customer, List.of(cartItem), BigDecimal.ZERO);
        sampleInvoice = new Invoice("FV/2026/08/1", LocalDate.of(2026, 8, 3), sampleOrder);
    }

    @Test
    void shouldSerializeInvoiceToString() {
        // when
        String result = serializer.serialize(sampleInvoice);

        // then
        assertThat(result)
                .contains("invoiceNumber=FV/2026/08/1")
                .contains("issueDate=2026-08-03")
                .contains("orderId=OR1");
    }

    @Test
    void shouldDeserializeInvoiceFromLines() {
        // given
        List<String> lines = List.of(
                "invoiceNumber=FV/2026/08/1",
                "issueDate=2026-08-03",
                "orderId=OR1"
        );
        when(orderRepository.findById("OR1")).thenReturn(Optional.of(sampleOrder));

        // when
        Invoice invoice = serializer.deserialize(lines);

        // then
        assertThat(invoice.getInvoiceNumber()).isEqualTo("FV/2026/08/1");
        assertThat(invoice.getIssueDate()).isEqualTo(LocalDate.of(2026, 8, 3));
        assertThat(invoice.getOrder().getOrderId()).isEqualTo("OR1");
    }

    @Test
    void shouldThrowOrderNotFoundExceptionWhenOrderNotFound() {
        // given
        List<String> lines = List.of(
                "invoiceNumber=FV/2026/08/1",
                "issueDate=2026-08-03",
                "orderId=OR_MISSING"
        );
        when(orderRepository.findById("OR_MISSING")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> serializer.deserialize(lines))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void shouldThrowCorruptedFileDataExceptionWhenKeyIsMissing() {
        // given
        List<String> incompleteLines = List.of(
                "invoiceNumber=FV/2026/08/1"
                // brak issueDate i orderId
        );

        // when & then
        assertThatThrownBy(() -> serializer.deserialize(incompleteLines))
                .isInstanceOf(CorruptedFileDataException.class)
                .hasMessageContaining("Missing required key in file data");
    }
}