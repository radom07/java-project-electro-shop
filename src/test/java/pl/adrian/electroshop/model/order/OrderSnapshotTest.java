package pl.adrian.electroshop.model.order;

import org.junit.jupiter.api.Test;
import pl.adrian.electroshop.model.product.configuration.NoConfiguration;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderSnapshotTest {

    @Test
    void shouldHoldDataCorrectly() {
        // given
        Instant now = Instant.now();
        OrderLine line = new OrderLine("E1", "Cable", new BigDecimal("10.00"), new NoConfiguration(), 2);
        List<OrderLine> items = List.of(line);

        // when
        OrderSnapshot snapshot = new OrderSnapshot(
                "OR1",
                now,
                "CU1",
                "Jan",
                "Kowalski",
                "jan@test.pl",
                OrderStatus.PLACED,
                items,
                new BigDecimal("20.00"),
                BigDecimal.ZERO
        );

        // then
        assertThat(snapshot.orderId()).isEqualTo("OR1");
        assertThat(snapshot.placedAt()).isEqualTo(now);
        assertThat(snapshot.customerId()).isEqualTo("CU1");
        assertThat(snapshot.customerFirstName()).isEqualTo("Jan");
        assertThat(snapshot.customerLastName()).isEqualTo("Kowalski");
        assertThat(snapshot.customerEmail()).isEqualTo("jan@test.pl");
        assertThat(snapshot.status()).isEqualTo(OrderStatus.PLACED);
        assertThat(snapshot.orderedItems()).hasSize(1).contains(line);
        assertThat(snapshot.subtotal()).isEqualTo(new BigDecimal("20.00"));
        assertThat(snapshot.discountAmount()).isEqualTo(BigDecimal.ZERO);
    }
}