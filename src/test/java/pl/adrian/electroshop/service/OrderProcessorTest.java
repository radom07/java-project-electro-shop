package pl.adrian.electroshop.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.adrian.electroshop.exception.InvalidOrderStatusTransitionException;
import pl.adrian.electroshop.exception.OrderNotFoundException;
import pl.adrian.electroshop.exception.ProductNotFoundException;
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
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
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

    private Order order;

    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.parse("2026-08-04T10:00:00Z"), ZoneId.of("Europe/Warsaw"));
        orderProcessor = new OrderProcessor(orderRepository, invoiceRepository, invoiceNumberGenerator, productManager, fixedClock);

        Customer customer = new Customer("CU1", "Jan", "Kowalski", "jan.kowalski@test.pl");
        CartItem cartItem = new Electronics("E1", "USB-C Cable", new BigDecimal("49.99"), 10)
                .toCartItem(new NoConfiguration(), 3);
        order = new Order("OR1", Instant.now(fixedClock), customer, List.of(cartItem), new BigDecimal("149.97"), BigDecimal.ZERO);
    }

    // processOrder

    @Test
    void shouldThrowExceptionWhenProcessingNullOrder() {
        // when & then
        assertThatThrownBy(() -> orderProcessor.processOrder(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("is marked non-null but is null");

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
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("Order not found");
    }

    @Test
    void shouldThrowExceptionWhenTransitionIsInvalid() {
        // given
        when(orderRepository.findById("OR1")).thenReturn(Optional.of(order));

        // when & then
        assertThatThrownBy(() -> orderProcessor.changeOrderStatus("OR1", OrderStatus.SHIPPED))
                .isInstanceOf(InvalidOrderStatusTransitionException.class)
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
    void shouldCancelOrderAndReleaseStockWhenOrderIsPlaced() {
        // given
        when(orderRepository.findById("OR1")).thenReturn(Optional.of(order));

        // when
        orderProcessor.cancelOrder("OR1");

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(productManager, times(1)).releaseStock("E1", 3);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void shouldThrowExceptionWhenCancellingNonExistentOrder() {
        // given
        when(orderRepository.findById("MISSING")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderProcessor.cancelOrder("MISSING"))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("Order not found");
    }

    @Test
    void shouldThrowExceptionWhenCancellingPaidOrder() {
        // given
        when(orderRepository.findById("OR1")).thenReturn(Optional.of(order));
        order.changeStatus(OrderStatus.PAID);

        // when & then
        assertThatThrownBy(() -> orderProcessor.cancelOrder("OR1"))
                .isInstanceOf(InvalidOrderStatusTransitionException.class)
                .hasMessageContaining("invoice correction is required");

        verify(productManager, never()).releaseStock(any(), anyInt());
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
                .isInstanceOf(InvalidOrderStatusTransitionException.class)
                .hasMessageContaining("Cannot cancel order in status: SHIPPED");

        verify(productManager, never()).releaseStock(any(), anyInt());
    }

    @Test
    void shouldThrowExceptionWhenCancellingAndProductNoLongerExists() {
        // given
        when(orderRepository.findById("OR1")).thenReturn(Optional.of(order));
        doThrow(new ProductNotFoundException("E1"))
                .when(productManager).releaseStock("E1", 3);

        // when & then
        assertThatThrownBy(() -> orderProcessor.cancelOrder("OR1"))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Product not found");

        verify(orderRepository, never()).save(any());
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PLACED); // status nietknięty
    }

    @Test
    void shouldReReserveAlreadyReleasedItemsWhenLaterItemFailsToRelease() {
        // given
        Customer customer = new Customer("CU1", "Jan", "Kowalski", "jan.kowalski@test.pl");
        CartItem firstItem = new Electronics("E1", "USB-C Cable", new BigDecimal("49.99"), 10)
                .toCartItem(new NoConfiguration(), 3);
        CartItem secondItem = new Electronics("E2", "Wireless Mouse", new BigDecimal("29.99"), 5)
                .toCartItem(new NoConfiguration(), 1);
        Order multiItemOrder = new Order("OR2", Instant.now(fixedClock), customer,
                List.of(firstItem, secondItem), new BigDecimal("179.94"), BigDecimal.ZERO);

        when(orderRepository.findById("OR2")).thenReturn(Optional.of(multiItemOrder));

        doNothing().when(productManager).releaseStock("E1", 3);
        doThrow(new ProductNotFoundException("E2"))
                .when(productManager).releaseStock("E2", 1);

        // when & then
        assertThatThrownBy(() -> orderProcessor.cancelOrder("OR2"))
                .isInstanceOf(ProductNotFoundException.class);

        verify(productManager, times(1)).releaseStock("E1", 3);
        verify(productManager, times(1)).reserveStock("E1", 3);
        assertThat(multiItemOrder.getStatus()).isEqualTo(OrderStatus.PLACED);
    }
}