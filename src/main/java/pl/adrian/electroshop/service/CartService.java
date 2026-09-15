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

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class CartService {

    @NonNull private final ProductManager productManager;
    @NonNull private final Cart cart;
    @NonNull private final Clock clock;

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
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }
        if (cart.isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        for (CartItem item : cart.getItems()) {
            Product product = productManager.getProduct(item.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(item.getProductId()));
            if (product.getQuantity() < item.getQuantity()) {
                throw new InsufficientStockException(product.getId());
            }
        }

        for (CartItem item : cart.getItems()) {
            Product product = productManager.getProduct(item.getProductId()).orElseThrow();
            product.setQuantity(product.getQuantity() - item.getQuantity());
            productManager.updateProduct(product);
        }

        Instant placedAt = Instant.now(clock);

        Order order = new Order(
                UUID.randomUUID().toString(),
                placedAt,
                customer,
                cart.getItems(),
                cart.getTotal()
        );

        cart.clear();

        return order;
    }
}