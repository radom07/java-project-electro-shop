package pl.adrian.electroshop.repository.file.serialization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.adrian.electroshop.exception.CorruptedFileDataException;
import pl.adrian.electroshop.model.customer.Customer;
import pl.adrian.electroshop.model.order.Order;
import pl.adrian.electroshop.model.order.OrderStatus;
import pl.adrian.electroshop.model.product.CartItem;
import pl.adrian.electroshop.model.product.Electronics;
import pl.adrian.electroshop.model.product.configuration.NoConfiguration;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderFileSerializerTest {

    private OrderFileSerializer serializer;
    private Order sampleOrder;

    @BeforeEach
    void setUp() {
        serializer = new OrderFileSerializer();
        Customer customer = new Customer("CU1", "Jan", "Kowalski", "jan.kowalski@test.pl");
        Electronics cable = new Electronics("E1", "USB-C Cable", new BigDecimal("49.99"), 100);
        CartItem cartItem = cable.toCartItem(new NoConfiguration(), 2);

        sampleOrder = new Order("OR1", Instant.parse("2026-08-03T12:00:00Z"), customer, List.of(cartItem), new BigDecimal("10.00"));
    }

    @Test
    void shouldSerializeOrderToString() {
        // when
        String result = serializer.serialize(sampleOrder);

        // then
        assertThat(result)
                .contains("orderId=OR1")
                .contains("customerId=CU1")
                .contains("customerFirstName=Jan")
                .contains("subtotal=99.98")
                .contains("discountAmount=10.00")
                .contains("items=1")
                .contains("item.0.productId=E1")
                .contains("item.0.configType=NONE");
    }

    @Test
    void shouldDeserializeOrderFromLines() {
        // given
        List<String> lines = List.of(
                "orderId=OR1",
                "placedAt=2026-08-04T10:00:00Z",
                "customerId=CU1",
                "customerFirstName=Jan",
                "customerLastName=Kowalski",
                "customerEmail=jan.kowalski@test.pl",
                "status=PLACED",
                "subtotal=99.98",
                "discountAmount=10.00",
                "items=1",
                "item.0.productId=E1",
                "item.0.productName=USB-C Cable",
                "item.0.unitPrice=49.99",
                "item.0.quantity=2",
                "item.0.configType=NONE"
        );

        // when
        Order order = serializer.deserialize(lines);

        // then
        assertThat(order.getOrderId()).isEqualTo("OR1");
        assertThat(order.getCustomerFirstName()).isEqualTo("Jan");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PLACED);
        assertThat(order.getSubtotal()).isEqualByComparingTo("99.98");
        assertThat(order.getDiscountAmount()).isEqualByComparingTo("10.00");
        assertThat(order.getTotalAmount()).isEqualByComparingTo("89.98");
        assertThat(order.getOrderedItems()).hasSize(1);
        assertThat(order.getOrderedItems().get(0).getProductId()).isEqualTo("E1");
    }

    @Test
    void shouldThrowCorruptedFileDataExceptionWhenKeyIsMissing() {
        // given
        List<String> incompleteLines = List.of(
                "orderId=OR1",
                // brak placedAt, subtotal, discountAmount
                "customerId=CU1"
        );

        // when & then
        assertThatThrownBy(() -> serializer.deserialize(incompleteLines))
                .isInstanceOf(CorruptedFileDataException.class)
                .hasMessageContaining("Missing required key in file data");
    }
}