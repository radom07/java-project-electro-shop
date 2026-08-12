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

    private final List<OrderLine> orderedItems;

    private final BigDecimal subtotal;
    private final BigDecimal discountAmount;
    private final BigDecimal totalAmount;

    public Order(@NonNull String orderId,
                 @NonNull Instant placedAt,
                 @NonNull Customer customer,
                 @NonNull List<CartItem> cartItems,
                 @NonNull BigDecimal subtotal,
                 @NonNull BigDecimal discountAmount) {

        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cannot create order with empty cart.");
        }
        if (discountAmount.compareTo(subtotal) > 0) {
            throw new IllegalArgumentException("Discount amount cannot exceed subtotal.");
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
        this.subtotal = subtotal;
        this.discountAmount = discountAmount;
        this.totalAmount = subtotal.subtract(discountAmount);
    }

    public Order(OrderSnapshot snapshot) {
        this.orderId = snapshot.orderId();
        this.placedAt = snapshot.placedAt();
        this.customerId = snapshot.customerId();
        this.customerFirstName = snapshot.customerFirstName();
        this.customerLastName = snapshot.customerLastName();
        this.customerEmail = snapshot.customerEmail();
        this.status = snapshot.status();
        this.orderedItems = List.copyOf(snapshot.orderedItems());
        this.subtotal = snapshot.subtotal();
        this.discountAmount = snapshot.discountAmount();
        this.totalAmount = this.subtotal.subtract(this.discountAmount);
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
        return String.format("Order [ID: %s, Placed at: %s, Customer: %s %s, Subtotal: %.2f zł, Discount: %.2f zł, " +
                        "Total: %.2f zł, Items Count: %d]",
                orderId, placedAt, customerFirstName, customerLastName, subtotal, discountAmount, totalAmount,
                orderedItems.size());
    }
}