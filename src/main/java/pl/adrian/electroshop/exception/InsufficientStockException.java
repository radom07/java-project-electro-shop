package pl.adrian.electroshop.exception;

public class InsufficientStockException extends ElectroShopException {
    public InsufficientStockException(String productId) {
        super("Not enough stock for product: " + productId);
    }
}
