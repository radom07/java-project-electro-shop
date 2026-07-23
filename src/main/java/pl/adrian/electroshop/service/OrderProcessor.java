package pl.adrian.electroshop.service;

import pl.adrian.electroshop.model.invoice.Invoice;
import pl.adrian.electroshop.model.order.Order;
import pl.adrian.electroshop.model.order.OrderLine;
import pl.adrian.electroshop.model.order.OrderStatus;
import pl.adrian.electroshop.model.product.Product;
import pl.adrian.electroshop.repository.InvoiceRepository;
import pl.adrian.electroshop.repository.OrderRepository;
import pl.adrian.electroshop.service.invoice.InvoiceNumberGenerator;

import java.time.LocalDate;

public class OrderProcessor {

    private final OrderRepository orderRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceNumberGenerator invoiceNumberGenerator;
    private final ProductManager productManager;

    public OrderProcessor(OrderRepository orderRepository,
                          InvoiceRepository invoiceRepository,
                          InvoiceNumberGenerator invoiceNumberGenerator, ProductManager productManager) {
        this.orderRepository = orderRepository;
        this.invoiceRepository = invoiceRepository;
        this.invoiceNumberGenerator = invoiceNumberGenerator;
        this.productManager = productManager;
    }

    public Invoice processOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }

        orderRepository.save(order);

        Invoice invoice = generateInvoice(order);
        invoiceRepository.save(invoice);

        return invoice;
    }

    private Invoice generateInvoice(Order order) {
        String invoiceNumber = invoiceNumberGenerator.generateNumber();
        return new Invoice(invoiceNumber, LocalDate.now(), order);
    }

    public void changeOrderStatus(String orderId, OrderStatus newStatus) {
        if (newStatus == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException(
                    "Use cancelOrder() to cancel an order — it also restores stock levels.");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        order.changeStatus(newStatus);
        orderRepository.save(order);
    }

    public void cancelOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        if (order.getStatus() != OrderStatus.PLACED) {
            if (order.getStatus() == OrderStatus.PAID) {
                throw new IllegalStateException(
                        "Cannot cancel a paid order automatically — an invoice correction is required first. Order: " + orderId);
            }
            throw new IllegalStateException("Cannot cancel order in status: " + order.getStatus() + ". Order: " + orderId);
        }

        for (OrderLine item : order.getOrderedItems()) {
            productManager.getProduct(item.getProductId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Product no longer exists: " + item.getProductId()));
        }

        for (OrderLine item : order.getOrderedItems()) {
            Product product = productManager.getProduct(item.getProductId()).orElseThrow();
            product.setQuantity(product.getQuantity() + item.getQuantity());
            productManager.updateProduct(product);
        }

        order.changeStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }
}
