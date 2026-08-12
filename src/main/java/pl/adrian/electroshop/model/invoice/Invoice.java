package pl.adrian.electroshop.model.invoice;

import lombok.Getter;
import lombok.NonNull;
import pl.adrian.electroshop.model.order.Order;

import java.time.LocalDate;

@Getter
public class Invoice {
    private final String invoiceNumber;
    private final LocalDate issueDate;
    private final Order order;
    // Na potrzeby tego ćwiczenia jest to wersja uproszczona faktury

    public Invoice(@NonNull String invoiceNumber,
                   @NonNull LocalDate issueDate,
                   @NonNull Order order) {
        this.invoiceNumber = invoiceNumber;
        this.issueDate = issueDate;
        this.order = order;
    }

    @Override
    public String toString() {
        return String.format("Invoice [Number: %s, Issue Date: %s, Order: %s, Total: %.2f zł]",
                invoiceNumber, issueDate, order.getOrderId(), order.getTotalAmount());
    }
}