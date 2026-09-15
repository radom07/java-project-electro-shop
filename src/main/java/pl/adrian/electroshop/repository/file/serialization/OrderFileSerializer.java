package pl.adrian.electroshop.repository.file.serialization;

import pl.adrian.electroshop.exception.CorruptedFileDataException;
import pl.adrian.electroshop.model.order.Order;
import pl.adrian.electroshop.model.order.OrderLine;
import pl.adrian.electroshop.model.order.OrderStatus;
import pl.adrian.electroshop.model.product.configuration.ComputerConfiguration;
import pl.adrian.electroshop.model.product.configuration.NoConfiguration;
import pl.adrian.electroshop.model.product.configuration.ProductConfiguration;
import pl.adrian.electroshop.model.product.configuration.SmartphoneConfiguration;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderFileSerializer {

    public String serialize(Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append("orderId=").append(order.getOrderId()).append("\n");
        sb.append("placedAt=").append(order.getPlacedAt()).append("\n");
        sb.append("customerId=").append(order.getCustomerId()).append("\n");
        sb.append("customerFirstName=").append(order.getCustomerFirstName()).append("\n");
        sb.append("customerLastName=").append(order.getCustomerLastName()).append("\n");
        sb.append("customerEmail=").append(order.getCustomerEmail()).append("\n");
        sb.append("status=").append(order.getStatus()).append("\n");
        sb.append("totalAmount=").append(order.getTotalAmount()).append("\n");

        List<OrderLine> items = order.getOrderedItems();
        sb.append("items=").append(items.size()).append("\n");
        for (int i = 0; i < items.size(); i++) {
            appendItem(sb, "item." + i, items.get(i));
        }
        return sb.toString();
    }

    private void appendItem(StringBuilder sb, String prefix, OrderLine item) {
        sb.append(prefix).append(".productId=").append(item.getProductId()).append("\n");
        sb.append(prefix).append(".productName=").append(item.getProductName()).append("\n");
        sb.append(prefix).append(".unitPrice=").append(item.getUnitPrice()).append("\n");
        sb.append(prefix).append(".quantity=").append(item.getQuantity()).append("\n");
        appendConfiguration(sb, prefix, item.getConfiguration());
    }

    private void appendConfiguration(StringBuilder sb, String prefix, ProductConfiguration configuration) {
        if (configuration instanceof ComputerConfiguration cc) {
            sb.append(prefix).append(".configType=COMPUTER\n");
            sb.append(prefix).append(".configCpu=").append(cc.cpu()).append("\n");
            sb.append(prefix).append(".configRam=").append(cc.ram()).append("\n");
        } else if (configuration instanceof SmartphoneConfiguration sc) {
            sb.append(prefix).append(".configType=SMARTPHONE\n");
            sb.append(prefix).append(".configColor=").append(sc.color()).append("\n");
            sb.append(prefix).append(".configBattery=").append(sc.batteryCapacity()).append("\n");
            sb.append(prefix).append(".configAccessories=").append(String.join(";", sc.accessories())).append("\n");
        } else {
            sb.append(prefix).append(".configType=NONE\n");
        }
    }

    public Order deserialize(List<String> lines) {
        Map<String, String> values = parseKeyValues(lines);

        try {
            int itemCount = Integer.parseInt(getRequiredValue(values, "items"));
            List<OrderLine> orderedItems = new ArrayList<>();
            for (int i = 0; i < itemCount; i++) {
                orderedItems.add(readOrderLine(values, "item." + i));
            }

            return Order.reconstruct(
                    getRequiredValue(values, "orderId"),
                    Instant.parse(getRequiredValue(values, "placedAt")),
                    getRequiredValue(values, "customerId"),
                    getRequiredValue(values, "customerFirstName"),
                    getRequiredValue(values, "customerLastName"),
                    getRequiredValue(values, "customerEmail"),
                    OrderStatus.valueOf(getRequiredValue(values, "status")),
                    orderedItems,
                    new BigDecimal(getRequiredValue(values, "totalAmount"))
            );
        } catch (Exception e) {
            throw new CorruptedFileDataException("Failed to parse order data", e);
        }
    }

    private OrderLine readOrderLine(Map<String, String> values, String prefix) {
        return new OrderLine(
                getRequiredValue(values, prefix + ".productId"),
                getRequiredValue(values, prefix + ".productName"),
                new BigDecimal(getRequiredValue(values, prefix + ".unitPrice")),
                readConfiguration(values, prefix),
                Integer.parseInt(getRequiredValue(values, prefix + ".quantity"))
        );
    }

    private ProductConfiguration readConfiguration(Map<String, String> values, String prefix) {
        String type = getRequiredValue(values, prefix + ".configType");
        return switch (type) {
            case "COMPUTER" -> new ComputerConfiguration(
                    getRequiredValue(values, prefix + ".configCpu"),
                    Integer.parseInt(getRequiredValue(values, prefix + ".configRam")));
            case "SMARTPHONE" -> {
                String raw = getRequiredValue(values, prefix + ".configAccessories");
                List<String> accessories = raw.isBlank() ? List.of() : List.of(raw.split(";"));
                yield new SmartphoneConfiguration(
                        getRequiredValue(values, prefix + ".configColor"),
                        Integer.parseInt(getRequiredValue(values, prefix + ".configBattery")),
                        accessories);
            }
            default -> new NoConfiguration();
        };
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
