package pl.adrian.electroshop.exception;

public class InvalidOrderStatusTransitionException extends ElectroShopException {
    public InvalidOrderStatusTransitionException(String message) {
        super(message);
    }
}
