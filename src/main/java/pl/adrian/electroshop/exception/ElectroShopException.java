package pl.adrian.electroshop.exception;

public abstract class ElectroShopException extends RuntimeException {
    protected ElectroShopException(String message) {
        super(message);
    }
}