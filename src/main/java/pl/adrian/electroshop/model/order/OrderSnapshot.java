package pl.adrian.electroshop.model.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderSnapshot(
        String orderId,
        Instant placedAt,
        String customerId,
        String customerFirstName,
        String customerLastName,
        String customerEmail,
        OrderStatus status,
        List<OrderLine> orderedItems,
        BigDecimal subtotal,
        BigDecimal discountAmount
) {
}