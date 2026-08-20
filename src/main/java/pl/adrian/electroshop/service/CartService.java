package pl.adrian.electroshop.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.adrian.electroshop.exception.InsufficientStockException;
import pl.adrian.electroshop.exception.ProductNotFoundException;
import pl.adrian.electroshop.model.cart.Cart;
import pl.adrian.electroshop.model.customer.Customer;
import pl.adrian.electroshop.model.order.Order;
import pl.adrian.electroshop.model.product.CartItem;
import pl.adrian.electroshop.model.product.Product;
import pl.adrian.electroshop.model.product.configuration.ProductConfiguration;
import pl.adrian.electroshop.service.concurrency.CompensatingAction;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/*
Design Pattern: FACADE (structural, GoF)
CartService serves as the single entry point to the "cart -> order" process
for the caller layer (CLI menu). Externally, it exposes only three simple methods:
#addToCart, #viewCart, and #placeOrder - while internally, the facade orchestrates
four collaborators that CustomerMenu is completely unaware of:

ProductManager — checking availability and reserving/releasing stock CompensatingAction to roll back
already completed reservations if an error occurs on a subsequent item
Cart - the current session's cart state
DiscountService - recalculating promo codes into discount amounts
Clock - the order placement timestamp

Without this facade, CustomerMenu would need to know the execution sequence itself
 */
@Slf4j
@RequiredArgsConstructor
public class CartService {

    @NonNull
    private final ProductManager productManager;
    @NonNull
    private final Cart cart;
    @NonNull
    private final Clock clock;
    @NonNull
    private final DiscountService discountService;

    public void addToCart(String productId, ProductConfiguration configuration, int quantity) {
        Product product = productManager.findProduct(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        int alreadyInCart = cart.findItem(productId, configuration)
                .map(CartItem::getQuantity)
                .orElse(0);

        if (product.getQuantity() < alreadyInCart + quantity) {
            log.warn("Insufficient stock for product {}: requested {}, available {}",
                    productId, alreadyInCart + quantity, product.getQuantity());
            throw new InsufficientStockException(productId);
        }

        CartItem cartItem = product.toCartItem(configuration, quantity);
        cart.addItem(cartItem);
        log.debug("Added product {} (x{}) to cart", productId, quantity);
    }

    public Order placeOrder(Customer customer) {
        return placeOrder(customer, null);
    }

    public Order placeOrder(@NonNull Customer customer, String discountCode) {
        if (cart.isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        reserveAllOrRollback(cart.getItems());

        BigDecimal subtotal = cart.getTotal();
        BigDecimal discountAmount = calculateDiscount(discountCode, subtotal);

        Instant placedAt = Instant.now(clock);

        Order order = Order.builder()
                .orderId(UUID.randomUUID().toString())
                .placedAt(placedAt)
                .customer(customer)
                .items(cart.getItems())
                .subtotal(subtotal)
                .discountAmount(discountAmount)
                .build();

        cart.clear();
        log.info("Order {} placed for customer {}", order.getOrderId(), customer.getCustomerId());
        return order;
    }

    private void reserveAllOrRollback(List<CartItem> items) {
        CompensatingAction.applyToAllOrCompensate(
                items,
                item -> productManager.reserveStock(item.getProductId(), item.getQuantity()),
                item -> productManager.releaseStock(item.getProductId(), item.getQuantity())
        );
    }

    private BigDecimal calculateDiscount(String discountCode, BigDecimal subtotal) {
        BigDecimal discountPercentage = discountService.getDiscountPercentage(discountCode)
                .orElse(BigDecimal.ZERO);
        return subtotal.multiply(discountPercentage);
    }

    public List<CartItem> viewCart() {
        return cart.getItems();
    }
}