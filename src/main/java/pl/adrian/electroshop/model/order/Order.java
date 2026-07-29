package pl.adrian.electroshop.model.order;

import lombok.Getter;
import lombok.NonNull;
import pl.adrian.electroshop.model.customer.Customer;
import pl.adrian.electroshop.model.product.CartItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class Order {
    private final String orderId;
    private final LocalDateTime placedAt;

    // Zmiana z referencji na Snapshot danych klienta z momentu składania zamówienia
    // Dzięki temu późniejsza zmiana profilu klienta nie wpływa na już złożone zamówienia.
    private final String customerId;
    private final String customerFirstName;
    private final String customerLastName;
    private final String customerEmail;
    private OrderStatus status = OrderStatus.PLACED;

    private final List<OrderLine> orderedItems; // zmiana na OrderLine z CartItem - zabezpieczenie niemutowalności
    // TODO: totalAmount nie jest walidowany względem orderedItems, do rozstrzygnięcia przy Task 12 (rabaty)
    private final BigDecimal totalAmount;

    public Order(@NonNull String orderId,
                 @NonNull LocalDateTime placedAt,
                 @NonNull Customer customer,
                 @NonNull List<CartItem> cartItems,
                 @NonNull BigDecimal totalAmount) {

        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cannot create order with empty cart.");
        }
        this.orderId = orderId;
        this.placedAt = placedAt;
        this.customerId = customer.getCustomerId();
        this.customerFirstName = customer.getFirstName();
        this.customerLastName = customer.getLastName();
        this.customerEmail = customer.getEmail();
        this.orderedItems = cartItems.stream()
                .map(OrderLine::new)
                .toList();
        this.totalAmount = totalAmount;
    }

    public void changeStatus(OrderStatus newStatus) {
        if (!status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    "Cannot change order status from " + status + " to " + newStatus);
        }
        this.status = newStatus;
    }

    @Override
    public String toString() {
        return String.format("Order [ID: %s, Placed at: %s, Customer: %s %s, Total: %.2f zł, Items Count: %d]",
                orderId, placedAt, customerFirstName, customerLastName, totalAmount, orderedItems.size());
    }
}
