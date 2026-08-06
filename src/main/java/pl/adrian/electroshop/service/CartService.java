package pl.adrian.electroshop.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import pl.adrian.electroshop.exception.InsufficientStockException;
import pl.adrian.electroshop.exception.ProductNotFoundException;
import pl.adrian.electroshop.model.cart.Cart;
import pl.adrian.electroshop.model.customer.Customer;
import pl.adrian.electroshop.model.order.Order;
import pl.adrian.electroshop.model.product.CartItem;
import pl.adrian.electroshop.model.product.Product;
import pl.adrian.electroshop.model.product.configuration.ProductConfiguration;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
        Product product = productManager.getProduct(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        int alreadyInCart = cart.findItem(productId, configuration)
                .map(CartItem::getQuantity)
                .orElse(0);

        if (product.getQuantity() < alreadyInCart + quantity) {
            throw new InsufficientStockException(productId);
        }

        CartItem cartItem = product.toCartItem(configuration, quantity); // walidacja
        cart.addItem(cartItem);
    }

    public List<CartItem> viewCart() {
        return cart.getItems();
    }

    public Order placeOrder(Customer customer) {
        return placeOrder(customer, null);
    }

    public Order placeOrder(Customer customer, String discountCode) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }
        if (cart.isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        List<CartItem> items = cart.getItems();
        List<CartItem> reserved = new ArrayList<>();
        try {
            for (CartItem item : items) {
                productManager.reserveStock(item.getProductId(), item.getQuantity());
                reserved.add(item);
            }
        } catch (RuntimeException e) {
            for (CartItem item : reserved) {
                productManager.releaseStock(item.getProductId(), item.getQuantity());
            }
            throw e;
        }
        BigDecimal subtotal = cart.getTotal();
        BigDecimal discountPercentage = discountService.getDiscountPercentage(discountCode)
                .orElse(BigDecimal.ZERO);
        BigDecimal discountAmount = subtotal.multiply(discountPercentage);

        Instant placedAt = Instant.now(clock);

        Order order = new Order(
                UUID.randomUUID().toString(),
                placedAt,
                customer,
                cart.getItems(),
                subtotal,
                discountAmount
        );

        cart.clear();

        return order;
    }
}