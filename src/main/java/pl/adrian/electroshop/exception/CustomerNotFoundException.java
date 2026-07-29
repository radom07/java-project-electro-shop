package pl.adrian.electroshop.exception;

public class CustomerNotFoundException extends ElectroShopException {
    public CustomerNotFoundException(String customerId) {
        super("Customer not found: " + customerId);
    }
}
