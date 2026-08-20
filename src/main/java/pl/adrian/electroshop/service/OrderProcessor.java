package pl.adrian.electroshop.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.adrian.electroshop.exception.InvalidOrderStatusTransitionException;
import pl.adrian.electroshop.exception.OrderNotFoundException;
import pl.adrian.electroshop.model.invoice.Invoice;
import pl.adrian.electroshop.model.order.Order;
import pl.adrian.electroshop.model.order.OrderLine;
import pl.adrian.electroshop.model.order.OrderStatus;
import pl.adrian.electroshop.repository.InvoiceRepository;
import pl.adrian.electroshop.repository.OrderRepository;
import pl.adrian.electroshop.service.concurrency.CompensatingAction;
import pl.adrian.electroshop.service.invoice.InvoiceNumberGenerator;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Design Pattern: FACADE (structural, GoF)
@Slf4j
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

    public Invoice processOrder(@NonNull Order order) {
        orderRepository.save(order);

        Invoice invoice = generateInvoice(order);
        invoiceRepository.save(invoice);

        log.info("Order {} processed, invoice {} generated", order.getOrderId(), invoice.getInvoiceNumber());
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
        log.info("Order {} status changed to {}", orderId, newStatus);
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
        releaseAllOrRollback(order);
        order.changeStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        log.info("Order {} cancelled", orderId);
    }

    private void releaseAllOrRollback(Order order) {
        CompensatingAction.applyToAllOrCompensate(
                order.getOrderedItems(),
                item -> productManager.releaseStock(item.getProductId(), item.getQuantity()),
                item -> productManager.reserveStock(item.getProductId(), item.getQuantity())
        );
    }
}