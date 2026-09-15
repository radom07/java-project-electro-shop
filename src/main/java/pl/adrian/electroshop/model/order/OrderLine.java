package pl.adrian.electroshop.model.order;

import lombok.Getter;
import pl.adrian.electroshop.model.product.CartItem;
import pl.adrian.electroshop.model.product.configuration.ProductConfiguration;

import java.math.BigDecimal;

/*
Dodałem tą klasę bo jest problem gdyby ktoś użył order.getOrderedItems().get(0).setQuantity
Można też byłoby usunąć settery z CartItem i operowanie setQty w koszuku postawić na oprcji reamove add
Ale to by bylo chyba kosztowniejsze na ten moment niż OrderLine
 */
@Getter
public final class OrderLine {
    private final String productId;
    private final String productName;
    private final BigDecimal unitPrice;
    private final ProductConfiguration configuration;
    private final int quantity;

    public OrderLine(CartItem cartItem) {
        this.productId = cartItem.getProductId();
        this.productName = cartItem.getProductName();
        this.unitPrice = cartItem.getUnitPrice();
        this.configuration = cartItem.getConfiguration();
        this.quantity = cartItem.getQuantity();
    }

    // Konstruktor do odtwarzania OrderLine z persystencji plikowej
    public OrderLine(String productId, String productName, BigDecimal unitPrice,
                     ProductConfiguration configuration, int quantity) {
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.configuration = configuration;
        this.quantity = quantity;
    }

    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    @Override
    public String toString() {
        return String.format("OrderLine [Product: %s, Unit price: %.2f zł, Quantity: %d, Subtotal: %.2f zł]",
                productName, unitPrice, quantity, getSubtotal());
    }
}
