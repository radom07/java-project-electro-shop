package pl.adrian.electroshop.exception;

public class OrderNotFoundException extends ElectroShopException {
    public OrderNotFoundException(String orderId) {
        super("Order not found: " + orderId);
    }
}
