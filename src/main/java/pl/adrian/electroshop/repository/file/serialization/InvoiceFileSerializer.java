package pl.adrian.electroshop.repository.file.serialization;

import pl.adrian.electroshop.exception.CorruptedFileDataException;
import pl.adrian.electroshop.exception.OrderNotFoundException;
import pl.adrian.electroshop.model.invoice.Invoice;
import pl.adrian.electroshop.model.order.Order;
import pl.adrian.electroshop.repository.OrderRepository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvoiceFileSerializer {

    private final OrderRepository orderRepository;

    public InvoiceFileSerializer(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public String serialize(Invoice invoice) {
        StringBuilder sb = new StringBuilder();
        sb.append("invoiceNumber=").append(invoice.getInvoiceNumber()).append("\n");
        sb.append("issueDate=").append(invoice.getIssueDate()).append("\n");
        sb.append("orderId=").append(invoice.getOrder().getOrderId()).append("\n");
        return sb.toString();
    }

    public Invoice deserialize(List<String> lines) {
        Map<String, String> values = parseKeyValues(lines);

        try {
            String invoiceNumber = getRequiredValue(values, "invoiceNumber");
            String orderId = getRequiredValue(values, "orderId");

            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new OrderNotFoundException(orderId));

            return new Invoice(invoiceNumber, LocalDate.parse(getRequiredValue(values, "issueDate")), order);
        } catch (Exception e) {
            if (e instanceof OrderNotFoundException) {
                throw e;
            }
            throw new CorruptedFileDataException("Failed to parse invoice data", e);
        }
    }

    private Map<String, String> parseKeyValues(List<String> lines) {
        Map<String, String> values = new HashMap<>();
        for (String line : lines) {
            if (line.isBlank()) {
                continue;
            }
            String[] parts = line.split("=", 2);
            values.put(parts[0], parts.length > 1 ? parts[1] : "");
        }
        return values;
    }

    private String getRequiredValue(Map<String, String> values, String key) {
        String value = values.get(key);
        if (value == null) {
            throw new CorruptedFileDataException("Missing required key in file data: " + key);
        }
        return value;
    }
}
