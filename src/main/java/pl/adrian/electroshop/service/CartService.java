package pl.adrian.electroshop.service;

import pl.adrian.electroshop.exception.InsufficientStockException;
import pl.adrian.electroshop.exception.ProductNotFoundException;
import pl.adrian.electroshop.model.cart.Cart;
import pl.adrian.electroshop.model.customer.Customer;
import pl.adrian.electroshop.model.order.Order;
import pl.adrian.electroshop.model.product.CartItem;
import pl.adrian.electroshop.model.product.Product;
import pl.adrian.electroshop.model.product.configuration.ProductConfiguration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class CartService {
    private final ProductManager productManager;
    private final Cart cart;

    public CartService(ProductManager productManager, Cart cart) {
        this.productManager = productManager;
        this.cart = cart;
    }

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

        // Walidacja dostępności wszystkich pozycji przed jakąkolwiek modyfikacją stanu
        for (CartItem item : cart.getItems()) {
            Product product = productManager.getProduct(item.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(item.getProductId()));
            if (product.getQuantity() < item.getQuantity()) {
                throw new InsufficientStockException(product.getId());
            }
        }

        // Aktualizacja stanów magazynowych
        for (CartItem item : cart.getItems()) {
            Product product = productManager.getProduct(item.getProductId()).orElseThrow();
            product.setQuantity(product.getQuantity() - item.getQuantity());
            productManager.updateProduct(product);
        }

        Order order = new Order(
                UUID.randomUUID().toString(),
                LocalDateTime.now(),
                customer,
                cart.getItems(),
                cart.getTotal()
        );

        cart.clear();

        return order;
    }
}