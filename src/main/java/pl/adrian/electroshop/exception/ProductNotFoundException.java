package pl.adrian.electroshop.exception;

public class ProductNotFoundException extends ElectroShopException {
    public ProductNotFoundException(String productId) {
        super("Product not found: " + productId);
    }
}