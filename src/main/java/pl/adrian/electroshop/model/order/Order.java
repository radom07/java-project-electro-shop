package pl.adrian.electroshop.model.order;

import lombok.Getter;
import lombok.NonNull;
import pl.adrian.electroshop.model.customer.Customer;
import pl.adrian.electroshop.model.product.CartItem;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class Order {
    private final String orderId;
    private final Customer customer;
    private final List<CartItem> orderedItems;
    private final BigDecimal totalAmount;

    public Order(@NonNull String orderId,
                 @NonNull Customer customer,
                 @NonNull List<CartItem> cartItems,
                 @NonNull BigDecimal totalAmount) {

        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cannot create order with empty cart.");
        }
        this.orderId = orderId;
        this.customer = customer;
        this.orderedItems = List.copyOf(cartItems);
        this.totalAmount = totalAmount;
    }

    @Override
    public String toString() {
        return String.format("Order [ID: %s, Customer: %s %s, Total: %.2f zł, Items Count: %d]",
                orderId, customer.getFirstName(), customer.getLastName(), totalAmount, orderedItems.size());
    }
}
