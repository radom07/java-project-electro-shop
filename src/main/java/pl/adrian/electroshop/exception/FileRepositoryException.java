package pl.adrian.electroshop.exception;

public class FileRepositoryException extends ElectroShopException {
    public FileRepositoryException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }
}
