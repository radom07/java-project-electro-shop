package pl.adrian.electroshop.exception;

public class CartItemNotFoundException extends ElectroShopException {
    public CartItemNotFoundException(String productId) {
        super("Item not found in cart: " + productId);
    }
}
