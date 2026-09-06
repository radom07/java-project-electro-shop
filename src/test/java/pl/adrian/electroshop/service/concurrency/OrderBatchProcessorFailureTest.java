package pl.adrian.electroshop.service.concurrency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.adrian.electroshop.model.customer.Customer;
import pl.adrian.electroshop.model.invoice.Invoice;
import pl.adrian.electroshop.model.order.Order;
import pl.adrian.electroshop.model.product.CartItem;
import pl.adrian.electroshop.model.product.Electronics;
import pl.adrian.electroshop.model.product.configuration.NoConfiguration;
import pl.adrian.electroshop.service.OrderProcessor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderBatchProcessorFailureTest {

    @Mock
    private OrderProcessor orderProcessor;

    private List<Order> orders;

    @BeforeEach
    void setUp() {
        Customer customer = new Customer("CU1", "Jan", "Kowalski", "jan@test.pl");
        orders = List.of(
                createOrder("OR1", customer),
                createOrder("OR2", customer)
        );
    }

    private Order createOrder(String orderId, Customer customer) {
        Electronics product = new Electronics(orderId + "-P", "Product", new BigDecimal("10.00"), 5);
        CartItem item = product.toCartItem(new NoConfiguration(), 1);
        return new Order(orderId, Instant.now(), customer, List.of(item), BigDecimal.ZERO);
    }

    @Test
    void processSequentiallyShouldPropagateFailureImmediatelyAndSkipRemainingOrders() {
        // given
        when(orderProcessor.processOrder(orders.get(0)))
                .thenThrow(new RuntimeException("Simulated payment gateway failure"));

        ExecutorService executor = Executors.newFixedThreadPool(1);
        OrderBatchProcessor processor = new OrderBatchProcessor(orderProcessor, executor);

        // when & then
        assertThatThrownBy(() -> processor.processSequentially(orders))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Simulated payment gateway failure");

        verify(orderProcessor, never()).processOrder(orders.get(1));

        executor.shutdown();
    }

    @Test
    void processConcurrentlyShouldWrapFailureAsIllegalStateException() throws InterruptedException {
        // given
        Invoice successfulInvoice = new Invoice("FV/2026/08/1", LocalDate.now(), orders.get(0));
        when(orderProcessor.processOrder(orders.get(0))).thenReturn(successfulInvoice);
        when(orderProcessor.processOrder(orders.get(1)))
                .thenThrow(new RuntimeException("Simulated payment gateway failure"));

        ExecutorService executor = Executors.newFixedThreadPool(2);
        OrderBatchProcessor processor = new OrderBatchProcessor(orderProcessor, executor);

        // when & then
        assertThatThrownBy(() -> processor.processConcurrently(orders))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Order processing failed")
                .hasCauseInstanceOf(RuntimeException.class);

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }

    @Test
    void processAsyncShouldPropagateFailureThroughCompletableFuture() {
        // given
        when(orderProcessor.processOrder(any()))
                .thenThrow(new RuntimeException("Simulated payment gateway failure"));

        ExecutorService executor = Executors.newFixedThreadPool(1);
        OrderBatchProcessor processor = new OrderBatchProcessor(orderProcessor, executor);

        // when
        CompletableFuture<List<Invoice>> future = processor.processAsync(List.of(orders.get(0)));

        // then
        assertThatThrownBy(future::join)
                .isInstanceOf(CompletionException.class)
                .hasCauseInstanceOf(RuntimeException.class);

        executor.shutdown();
    }

    @Test
    void processConcurrentlyShouldStillCompleteSuccessfulOrdersDespiteOneFailing() throws InterruptedException {
        // given
        Invoice successfulInvoice = new Invoice("FV/2026/08/1", LocalDate.now(), orders.get(0));
        when(orderProcessor.processOrder(orders.get(0))).thenReturn(successfulInvoice);
        when(orderProcessor.processOrder(orders.get(1)))
                .thenThrow(new RuntimeException("Simulated payment gateway failure"));

        ExecutorService executor = Executors.newFixedThreadPool(2);
        OrderBatchProcessor processor = new OrderBatchProcessor(orderProcessor, executor);

        // when
        assertThatThrownBy(() -> processor.processConcurrently(orders))
                .isInstanceOf(IllegalStateException.class);

        // then
        verify(orderProcessor, times(1)).processOrder(orders.get(0));
        verify(orderProcessor, times(1)).processOrder(orders.get(1));

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }
}