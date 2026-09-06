package pl.adrian.electroshop.model.order;

import lombok.Getter;
import lombok.NonNull;
import pl.adrian.electroshop.exception.InvalidOrderStatusTransitionException;
import pl.adrian.electroshop.model.customer.Customer;
import pl.adrian.electroshop.model.product.CartItem;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

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
                 @NonNull BigDecimal discountAmount) {

        List<OrderLine> orderedItems = cartItems.stream()
                .map(OrderLine::new)
                .toList();
        BigDecimal subtotal = computeSubtotal(orderedItems);

        validate(orderedItems, subtotal, discountAmount);

        this.orderId = orderId;
        this.placedAt = placedAt;
        this.customerId = customer.getCustomerId();
        this.customerFirstName = customer.getFirstName();
        this.customerLastName = customer.getLastName();
        this.customerEmail = customer.getEmail();
        this.orderedItems = orderedItems;
        this.subtotal = subtotal;
        this.discountAmount = discountAmount;
        this.totalAmount = subtotal.subtract(discountAmount);
    }

    public Order(OrderSnapshot snapshot) {
        List<OrderLine> orderedItems = List.copyOf(snapshot.orderedItems());
        BigDecimal subtotal = computeSubtotal(orderedItems);

        validateSubtotalMatchesItems(subtotal, snapshot.subtotal());
        validate(orderedItems, subtotal, snapshot.discountAmount());

        this.orderId = snapshot.orderId();
        this.placedAt = snapshot.placedAt();
        this.customerId = snapshot.customerId();
        this.customerFirstName = snapshot.customerFirstName();
        this.customerLastName = snapshot.customerLastName();
        this.customerEmail = snapshot.customerEmail();
        this.status = snapshot.status();
        this.orderedItems = orderedItems;
        this.subtotal = subtotal;
        this.discountAmount = snapshot.discountAmount();
        this.totalAmount = subtotal.subtract(this.discountAmount);
    }

    private static BigDecimal computeSubtotal(List<OrderLine> orderedItems) {
        return orderedItems.stream()
                .map(OrderLine::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static void validateSubtotalMatchesItems(BigDecimal computedSubtotal, BigDecimal declaredSubtotal) {
        if (computedSubtotal.compareTo(declaredSubtotal) != 0) {
            throw new IllegalArgumentException(
                    "Persisted subtotal (" + declaredSubtotal + ") does not match sum of order items (" + computedSubtotal + ").");
        }
    }

    private static void validate(List<OrderLine> orderedItems, BigDecimal subtotal, BigDecimal discountAmount) {
        if (orderedItems.isEmpty()) {
            throw new IllegalArgumentException("Cannot create order with empty cart.");
        }
        if (discountAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Discount amount cannot be negative.");
        }
        if (discountAmount.compareTo(subtotal) > 0) {
            throw new IllegalArgumentException("Discount amount cannot exceed subtotal.");
        }
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

    /*
    Design Pattern: BUILDER (creational, GoF)
    The Builder DOES NOT duplicate business rules - build() calls the standard Order constructor, so validations
    like "cart cannot be empty" and "discount cannot exceed subtotal" still live in a single place
    The old positional constructor remains available (including for existing tests) - the Builder is
    an additional, safer way to create the object, not a replacement for it.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String orderId;
        private Instant placedAt;
        private Customer customer;
        private List<CartItem> cartItems;
        private BigDecimal discountAmount = BigDecimal.ZERO;

        private Builder() {
        }

        public Builder orderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder placedAt(Instant placedAt) {
            this.placedAt = placedAt;
            return this;
        }

        public Builder customer(Customer customer) {
            this.customer = customer;
            return this;
        }

        public Builder items(List<CartItem> cartItems) {
            this.cartItems = cartItems;
            return this;
        }

        public Builder discountAmount(BigDecimal discountAmount) {
            this.discountAmount = discountAmount;
            return this;
        }

        public Order build() {
            Objects.requireNonNull(orderId, "orderId is required");
            Objects.requireNonNull(placedAt, "placedAt is required");
            Objects.requireNonNull(customer, "customer is required");
            Objects.requireNonNull(cartItems, "cartItems is required");
            return new Order(orderId, placedAt, customer, cartItems, discountAmount);
        }
    }
}