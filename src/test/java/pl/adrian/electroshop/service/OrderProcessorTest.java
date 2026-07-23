package pl.adrian.electroshop.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.adrian.electroshop.model.customer.Customer;
import pl.adrian.electroshop.model.invoice.Invoice;
import pl.adrian.electroshop.model.order.Order;
import pl.adrian.electroshop.model.order.OrderStatus;
import pl.adrian.electroshop.model.product.CartItem;
import pl.adrian.electroshop.model.product.Electronics;
import pl.adrian.electroshop.model.product.Product;
import pl.adrian.electroshop.model.product.configuration.NoConfiguration;
import pl.adrian.electroshop.repository.InvoiceRepository;
import pl.adrian.electroshop.repository.OrderRepository;
import pl.adrian.electroshop.service.invoice.InvoiceNumberGenerator;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderProcessorTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private InvoiceNumberGenerator invoiceNumberGenerator;

    @Mock
    private ProductManager productManager;

    private OrderProcessor orderProcessor;

    private Product cable;
    private Order order;

    @BeforeEach
    void setUp() {
        orderProcessor = new OrderProcessor(orderRepository, invoiceRepository, invoiceNumberGenerator, productManager);

        cable = new Electronics("E1", "USB-C Cable", new BigDecimal("49.99"), 7); // 10 - 3 sprzedane
        Customer customer = new Customer("CU1", "Jan", "Kowalski", "jan.kowalski@test.pl");
        CartItem cartItem = new Electronics("E1", "USB-C Cable", new BigDecimal("49.99"), 10)
                .toCartItem(new NoConfiguration(), 3);
        order = new Order("OR1", customer, List.of(cartItem), new BigDecimal("149.97"));
    }

    // processOrder

    @Test
    void shouldThrowExceptionWhenProcessingNullOrder() {
        // when & then
        assertThatThrownBy(() -> orderProcessor.processOrder(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Order cannot be null");

        verifyNoInteractions(orderRepository, invoiceRepository, invoiceNumberGenerator);
    }

    @Test
    void shouldSaveOrderAndGenerateInvoiceWhenProcessing() {
        // given
        when(invoiceNumberGenerator.generateNumber()).thenReturn("FV/2026/07/1");

        // when
        Invoice invoice = orderProcessor.processOrder(order);

        // then
        verify(orderRepository, times(1)).save(order);
        assertThat(invoice.getInvoiceNumber()).isEqualTo("FV/2026/07/1");
        assertThat(invoice.getOrder()).isEqualTo(order);
        verify(invoiceRepository, times(1)).save(invoice);
    }

    @Test
    void shouldNotChangeOrderStatusWhenProcessing() {
        // given
        when(invoiceNumberGenerator.generateNumber()).thenReturn("FV/2026/07/1");

        // when
        orderProcessor.processOrder(order);

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PLACED);
    }

    // changeOrderStatus

    @Test
    void shouldChangeOrderStatusWhenTransitionIsValid() {
        // given
        when(orderRepository.findById("OR1")).thenReturn(Optional.of(order));

        // when
        orderProcessor.changeOrderStatus("OR1", OrderStatus.PAID);

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void shouldThrowExceptionWhenOrderNotFoundForStatusChange() {
        // given
        when(orderRepository.findById("MISSING")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderProcessor.changeOrderStatus("MISSING", OrderStatus.PAID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Order not found");
    }

    @Test
    void shouldThrowExceptionWhenTransitionIsInvalid() {
        // given
        when(orderRepository.findById("OR1")).thenReturn(Optional.of(order));

        // when & then
        assertThatThrownBy(() -> orderProcessor.changeOrderStatus("OR1", OrderStatus.SHIPPED))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot change order status");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldRejectChangingStatusToCancelledDirectly() {
        // when & then
        assertThatThrownBy(() -> orderProcessor.changeOrderStatus("OR1", OrderStatus.CANCELLED))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Use cancelOrder()");

        verifyNoInteractions(orderRepository);
    }

    // cancelOrder

    @Test
    void shouldCancelOrderAndRestoreStockWhenOrderIsPlaced() {
        // given
        when(orderRepository.findById("OR1")).thenReturn(Optional.of(order));
        when(productManager.getProduct("E1")).thenReturn(Optional.of(cable));

        // when
        orderProcessor.cancelOrder("OR1");

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(cable.getQuantity()).isEqualTo(10); // 7 + 3 przywrócone
        verify(productManager, times(1)).updateProduct(cable);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void shouldThrowExceptionWhenCancellingNonExistentOrder() {
        // given
        when(orderRepository.findById("MISSING")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderProcessor.cancelOrder("MISSING"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Order not found");
    }

    @Test
    void shouldThrowExceptionWhenCancellingPaidOrder() {
        // given
        when(orderRepository.findById("OR1")).thenReturn(Optional.of(order));
        order.changeStatus(OrderStatus.PAID);

        // when & then
        assertThatThrownBy(() -> orderProcessor.cancelOrder("OR1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("invoice correction is required");

        verify(productManager, never()).updateProduct(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenCancellingShippedOrder() {
        // given
        when(orderRepository.findById("OR1")).thenReturn(Optional.of(order));
        order.changeStatus(OrderStatus.PAID);
        order.changeStatus(OrderStatus.SHIPPED);

        // when & then
        assertThatThrownBy(() -> orderProcessor.cancelOrder("OR1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot cancel order in status: SHIPPED");

        verify(productManager, never()).updateProduct(any());
    }

    @Test
    void shouldThrowExceptionWhenCancellingAndProductNoLongerExists() {
        // given
        when(orderRepository.findById("OR1")).thenReturn(Optional.of(order));
        when(productManager.getProduct("E1")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderProcessor.cancelOrder("OR1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Product no longer exists");

        verify(productManager, never()).updateProduct(any());
        verify(orderRepository, never()).save(any());
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PLACED); // status nietknięty
    }
}