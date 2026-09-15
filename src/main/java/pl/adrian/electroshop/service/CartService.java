package pl.adrian.electroshop.service;

import pl.adrian.electroshop.model.cart.Cart;
import pl.adrian.electroshop.model.product.CartItem;
import pl.adrian.electroshop.model.product.Product;
import pl.adrian.electroshop.model.product.configuration.ProductConfiguration;

import java.util.List;

public class CartService {
    private final ProductManager productManager;
    private final Cart cart;

    public CartService(ProductManager productManager, Cart cart) {
        this.productManager = productManager;
        this.cart = cart;
    }

    public void addToCart(String productId, ProductConfiguration configuration, int quantity) {
        Product product = productManager.getProduct(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        int alreadyInCart = cart.findItem(productId, configuration)
                .map(CartItem::getQuantity)
                .orElse(0);

        if (product.getQuantity() < alreadyInCart + quantity) {
            throw new IllegalStateException("Not enough stock for product: " + productId);
        }

        CartItem cartItem = product.toCartItem(configuration, quantity); // walidacja
        cart.addItem(cartItem);
    }

    public List<CartItem> viewCart() {
        return cart.getItems();
    }

    /*
     Finalizuje zamówienie: weryfikuje dostępność wszystkich pozycji, aktualizuje stany magazynowe i czyści koszyk.
     Task 5: utworzenie i zwrócenie obiektu Order na podstawie cart.getItems() i cart.getTotal(), zanim koszyk zostanie wyczyszczony.
     */
    public void placeOrder() {
        if (cart.isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        // Walidacja dostępności wszystkich pozycji przed modyfikacją stanu magazynowego
        for (CartItem item : cart.getItems()) {
            Product product = productManager.getProduct(item.getProductId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Product no longer available: " + item.getProductId()));
            if (product.getQuantity() < item.getQuantity()) {
                throw new IllegalStateException("Not enough stock for product: " + product.getId());
            }
        }

        // Aktualizacja stanów magazynowych
        for (CartItem item : cart.getItems()) {
            Product product = productManager.getProduct(item.getProductId()).orElseThrow();
            product.setQuantity(product.getQuantity() - item.getQuantity());
            productManager.updateProduct(product);
        }

        cart.clear();
    }
}

