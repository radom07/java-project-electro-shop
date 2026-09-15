package pl.adrian.electroshop.model.cart;

import lombok.Getter;
import lombok.NonNull;
import pl.adrian.electroshop.exception.CartItemNotFoundException;
import pl.adrian.electroshop.model.product.CartItem;
import pl.adrian.electroshop.model.product.configuration.ProductConfiguration;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.UUID.randomUUID;

public class Cart {

    @Getter
    private final String cartId = randomUUID().toString();

    private final List<CartItem> items = new ArrayList<>();

    public void addItem(@NonNull CartItem newItem) {
        findItem(newItem.getProductId(), newItem.getConfiguration())
                .ifPresentOrElse(
                        existing -> existing.increaseQuantity(newItem.getQuantity()),
                        () -> items.add(newItem)
                );
    }

    public void removeItem(String productId, ProductConfiguration configuration) {
        items.removeIf(item -> item.getProductId().equals(productId)
                && item.getConfiguration().equals(configuration));
    }

    public void removeAllForProduct(String productId) {
        items.removeIf(item -> item.getProductId().equals(productId));
    }

    public void updateQuantity(String productId, ProductConfiguration configuration, int quantity) {
        CartItem item = findItem(productId, configuration)
                .orElseThrow(() -> new CartItemNotFoundException(productId));
        item.setQuantity(quantity);
    }

    public Optional<CartItem> findItem(String productId, ProductConfiguration configuration) {
        return items.stream()
                .filter(item -> item.getProductId().equals(productId)
                        && item.getConfiguration().equals(configuration))
                .findFirst();
    }

    public List<CartItem> getItems() {
        return List.copyOf(items);
    }

    public BigDecimal getTotal() {
        return items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void clear() {
        items.clear();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public int getItemCount() {
        return items.size();
    }
}