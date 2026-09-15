package pl.adrian.electroshop.model.order;

import lombok.Getter;
import lombok.NonNull;
import pl.adrian.electroshop.exception.InvalidOrderStatusTransitionException;
import pl.adrian.electroshop.model.customer.Customer;
import pl.adrian.electroshop.model.product.CartItem;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
public class Order {
    private final String orderId;
    private final Instant placedAt;

    private final String customerId;
    private final String customerFirstName;
    private final String customerLastName;
    private final String customerEmail;
    private OrderStatus status = OrderStatus.PLACED;

    private final List<OrderLine> orderedItems; // zmiana na OrderLine z CartItem - zabezpieczenie niemutowalności
    // TODO: totalAmount nie jest walidowany względem orderedItems, do rozstrzygnięcia przy Task 12 (rabaty)
    private final BigDecimal totalAmount;

    public Order(@NonNull String orderId,
                 @NonNull Instant placedAt,
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

    // Konstruktor do odtwarzania OrderLine z persystencji plikowej
    private Order(String orderId, Instant placedAt, String customerId,
                  String customerFirstName, String customerLastName, String customerEmail,
                  OrderStatus status, List<OrderLine> orderedItems, BigDecimal totalAmount) {
        this.orderId = orderId;
        this.placedAt = placedAt;
        this.customerId = customerId;
        this.customerFirstName = customerFirstName;
        this.customerLastName = customerLastName;
        this.customerEmail = customerEmail;
        this.status = status;
        this.orderedItems = List.copyOf(orderedItems);
        this.totalAmount = totalAmount;
    }

    public static Order reconstruct(String orderId, Instant placedAt,
                                    String customerId, String customerFirstName,
                                    String customerLastName, String customerEmail,
                                    OrderStatus status, List<OrderLine> orderedItems,
                                    BigDecimal totalAmount) {
        return new Order(orderId, placedAt, customerId, customerFirstName, customerLastName,
                customerEmail, status, orderedItems, totalAmount);
    }

    public void changeStatus(OrderStatus newStatus) {
        if (!status.canTransitionTo(newStatus)) {
            throw new InvalidOrderStatusTransitionException(
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
