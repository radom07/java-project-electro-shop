package pl.adrian.electroshop.repository.file.serialization;

import lombok.extern.slf4j.Slf4j;
import pl.adrian.electroshop.exception.CorruptedFileDataException;
import pl.adrian.electroshop.model.order.Order;
import pl.adrian.electroshop.model.order.OrderLine;
import pl.adrian.electroshop.model.order.OrderSnapshot;
import pl.adrian.electroshop.model.order.OrderStatus;
import pl.adrian.electroshop.model.product.configuration.ComputerConfiguration;
import pl.adrian.electroshop.model.product.configuration.NoConfiguration;
import pl.adrian.electroshop.model.product.configuration.ProductConfiguration;
import pl.adrian.electroshop.model.product.configuration.SmartphoneConfiguration;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class OrderFileSerializer {

    private enum ConfigType {
        COMPUTER, SMARTPHONE, NONE
    }

    public String serialize(Order order) {
        StringBuilder builder = new StringBuilder();
        appendField(builder, "orderId", order.getOrderId());
        appendField(builder, "placedAt", order.getPlacedAt());
        appendField(builder, "customerId", order.getCustomerId());
        appendField(builder, "customerFirstName", order.getCustomerFirstName());
        appendField(builder, "customerLastName", order.getCustomerLastName());
        appendField(builder, "customerEmail", order.getCustomerEmail());
        appendField(builder, "status", order.getStatus());
        appendField(builder, "subtotal", order.getSubtotal());
        appendField(builder, "discountAmount", order.getDiscountAmount());

        List<OrderLine> items = order.getOrderedItems();
        appendField(builder, "items", items.size());
        for (int i = 0; i < items.size(); i++) {
            appendItem(builder, "item." + i, items.get(i));
        }
        return builder.toString();
    }

    private void appendItem(StringBuilder builder, String prefix, OrderLine item) {
        appendField(builder, prefix + ".productId", item.getProductId());
        appendField(builder, prefix + ".productName", item.getProductName());
        appendField(builder, prefix + ".unitPrice", item.getUnitPrice());
        appendField(builder, prefix + ".quantity", item.getQuantity());
        appendConfiguration(builder, prefix, item.getConfiguration());
    }

    private void appendConfiguration(StringBuilder builder, String prefix, ProductConfiguration configuration) {
        if (configuration instanceof ComputerConfiguration cc) {
            appendField(builder, prefix + ".configType", ConfigType.COMPUTER);
            appendField(builder, prefix + ".configCpu", cc.cpu());
            appendField(builder, prefix + ".configRam", cc.ram());
        } else if (configuration instanceof SmartphoneConfiguration sc) {
            appendField(builder, prefix + ".configType", ConfigType.SMARTPHONE);
            appendField(builder, prefix + ".configColor", sc.color());
            appendField(builder, prefix + ".configBattery", sc.batteryCapacity());
            appendField(builder, prefix + ".configAccessories", String.join(";", sc.accessories()));
        } else {
            appendField(builder, prefix + ".configType", ConfigType.NONE);
        }
    }

    private void appendField(StringBuilder builder, String key, Object value) {
        builder.append(key).append('=').append(value).append('\n');
    }

    public Order deserialize(List<String> lines) {
        KeyValueLines parsedData = KeyValueLines.parse(lines);

        try {
            int itemCount = Integer.parseInt(parsedData.getRequired("items"));
            List<OrderLine> orderedItems = new ArrayList<>();
            for (int i = 0; i < itemCount; i++) {
                orderedItems.add(readOrderLine(parsedData, "item." + i));
            }

            OrderSnapshot snapshot = new OrderSnapshot(
                    parsedData.getRequired("orderId"),
                    Instant.parse(parsedData.getRequired("placedAt")),
                    parsedData.getRequired("customerId"),
                    parsedData.getRequired("customerFirstName"),
                    parsedData.getRequired("customerLastName"),
                    parsedData.getRequired("customerEmail"),
                    OrderStatus.valueOf(parsedData.getRequired("status")),
                    orderedItems,
                    new BigDecimal(parsedData.getRequired("subtotal")),
                    new BigDecimal(parsedData.getRequired("discountAmount"))
            );

            return new Order(snapshot);

        } catch (DateTimeParseException | IllegalArgumentException e) {
            log.warn("Failed to parse order data due to invalid format", e);
            throw new CorruptedFileDataException("Failed to parse order data due to invalid format", e);
        }
    }

    private OrderLine readOrderLine(KeyValueLines parsedData, String prefix) {
        return new OrderLine(
                parsedData.getRequired(prefix + ".productId"),
                parsedData.getRequired(prefix + ".productName"),
                new BigDecimal(parsedData.getRequired(prefix + ".unitPrice")),
                readConfiguration(parsedData, prefix),
                Integer.parseInt(parsedData.getRequired(prefix + ".quantity"))
        );
    }

    private ProductConfiguration readConfiguration(KeyValueLines parsedData, String prefix) {
        ConfigType type = ConfigType.valueOf(parsedData.getRequired(prefix + ".configType"));
        return switch (type) {
            case COMPUTER -> new ComputerConfiguration(
                    parsedData.getRequired(prefix + ".configCpu"),
                    Integer.parseInt(parsedData.getRequired(prefix + ".configRam")));
            case SMARTPHONE -> {
                String raw = parsedData.getRequired(prefix + ".configAccessories");
                List<String> accessories = raw.isBlank() ? List.of() : List.of(raw.split(";"));
                yield new SmartphoneConfiguration(
                        parsedData.getRequired(prefix + ".configColor"),
                        Integer.parseInt(parsedData.getRequired(prefix + ".configBattery")),
                        accessories);
            }
            case NONE -> new NoConfiguration();
        };
    }
}