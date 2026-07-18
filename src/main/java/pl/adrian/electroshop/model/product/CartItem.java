package pl.adrian.electroshop.model.product;

import pl.adrian.electroshop.model.product.configuration.ProductConfiguration;

import java.math.BigDecimal;

public class CartItem {
    private final String productId;
    private final String productName;
    private final BigDecimal unitPrice;
    private final ProductConfiguration configuration;
    private int quantity;

    CartItem(String productId, String productName, BigDecimal unitPrice,
             ProductConfiguration configuration, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.configuration = configuration;
        this.quantity = quantity;
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public ProductConfiguration getConfiguration() {
        return configuration;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        this.quantity = quantity;
    }

    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}