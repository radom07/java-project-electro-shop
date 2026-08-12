package pl.adrian.electroshop.repository.file.serialization;

import pl.adrian.electroshop.exception.CorruptedFileDataException;
import pl.adrian.electroshop.exception.OrderNotFoundException;
import pl.adrian.electroshop.model.invoice.Invoice;
import pl.adrian.electroshop.model.order.Order;
import pl.adrian.electroshop.repository.OrderRepository;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class InvoiceFileSerializer {

    private final OrderRepository orderRepository;

    public InvoiceFileSerializer(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public String serialize(Invoice invoice) {
        StringBuilder builder = new StringBuilder();
        builder.append("invoiceNumber=").append(invoice.getInvoiceNumber()).append("\n");
        builder.append("issueDate=").append(invoice.getIssueDate()).append("\n");
        builder.append("orderId=").append(invoice.getOrder().getOrderId()).append("\n");
        return builder.toString();
    }

    public Invoice deserialize(List<String> lines) {
        KeyValueLines parsedData = KeyValueLines.parse(lines);

        String invoiceNumber = parsedData.getRequired("invoiceNumber");
        String orderId = parsedData.getRequired("orderId");

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        try {
            LocalDate issueDate = LocalDate.parse(parsedData.getRequired("issueDate"));
            return new Invoice(invoiceNumber, issueDate, order);
        } catch (DateTimeParseException e) {
            throw new CorruptedFileDataException("Failed to parse invoice data: invalid date format", e);
        }
    }
}