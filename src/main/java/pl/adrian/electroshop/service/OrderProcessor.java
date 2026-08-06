package pl.adrian.electroshop.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import pl.adrian.electroshop.exception.InvalidOrderStatusTransitionException;
import pl.adrian.electroshop.exception.OrderNotFoundException;
import pl.adrian.electroshop.model.invoice.Invoice;
import pl.adrian.electroshop.model.order.Order;
import pl.adrian.electroshop.model.order.OrderLine;
import pl.adrian.electroshop.model.order.OrderStatus;
import pl.adrian.electroshop.repository.InvoiceRepository;
import pl.adrian.electroshop.repository.OrderRepository;
import pl.adrian.electroshop.service.invoice.InvoiceNumberGenerator;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class OrderProcessor {

    @NonNull
    private final OrderRepository orderRepository;
    @NonNull
    private final InvoiceRepository invoiceRepository;
    @NonNull
    private final InvoiceNumberGenerator invoiceNumberGenerator;
    @NonNull
    private final ProductManager productManager;
    @NonNull
    private final Clock clock;

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
        return new Invoice(invoiceNumber, LocalDate.now(clock), order);
    }

    public void changeOrderStatus(String orderId, OrderStatus newStatus) {
        if (newStatus == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException(
                    "Use cancelOrder() to cancel an order — it also restores stock levels.");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        order.changeStatus(newStatus);
        orderRepository.save(order);
    }

    public void cancelOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() != OrderStatus.PLACED) {
            if (order.getStatus() == OrderStatus.PAID) {
                throw new InvalidOrderStatusTransitionException(
                        "Cannot cancel a paid order automatically — an invoice correction is required first. Order: " + orderId);
            }
            throw new InvalidOrderStatusTransitionException("Cannot cancel order in status: " + order.getStatus() + ". Order: " + orderId);
        }

        List<OrderLine> released = new ArrayList<>();
        try {
            for (OrderLine item : order.getOrderedItems()) {
                productManager.releaseStock(item.getProductId(), item.getQuantity());
                released.add(item);
            }
        } catch (RuntimeException e) {
            for (OrderLine item : released) {
                productManager.reserveStock(item.getProductId(), item.getQuantity());
            }
            throw e;
        }

        order.changeStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }
}
