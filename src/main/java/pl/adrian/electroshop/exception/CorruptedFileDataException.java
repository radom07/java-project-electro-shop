package pl.adrian.electroshop.exception;

public class CorruptedFileDataException extends ElectroShopException {
    public CorruptedFileDataException(String message) {
        super(message);
    }

    public CorruptedFileDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
