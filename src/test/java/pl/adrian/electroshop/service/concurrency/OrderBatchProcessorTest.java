package pl.adrian.electroshop.service.concurrency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.adrian.electroshop.model.customer.Customer;
import pl.adrian.electroshop.model.invoice.Invoice;
import pl.adrian.electroshop.model.order.Order;
import pl.adrian.electroshop.model.product.CartItem;
import pl.adrian.electroshop.model.product.Electronics;
import pl.adrian.electroshop.model.product.configuration.NoConfiguration;
import pl.adrian.electroshop.repository.inmemory.InMemoryInvoiceRepository;
import pl.adrian.electroshop.repository.inmemory.InMemoryOrderRepository;
import pl.adrian.electroshop.repository.inmemory.InMemoryProductRepository;
import pl.adrian.electroshop.service.OrderProcessor;
import pl.adrian.electroshop.service.ProductManager;
import pl.adrian.electroshop.service.invoice.SequentialInvoiceNumberGenerator;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class OrderBatchProcessorTest {

    private static final int NUMBER_OF_TEST_ORDERS = 50;
    private static final long SIMULATED_DELAY_MILLIS = 150;
    private static final Runnable DELAY_HOOK = () -> {
        try {
            Thread.sleep(SIMULATED_DELAY_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Simulated delay interrupted", e);
        }
    };

    private OrderProcessor orderProcessor;
    private List<Order> orders;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-08-05T10:00:00Z"), ZoneId.of("Europe/Warsaw"));

        orderProcessor = new OrderProcessor(
                new InMemoryOrderRepository(),
                new InMemoryInvoiceRepository(),
                new SequentialInvoiceNumberGenerator(fixedClock),
                new ProductManager(new InMemoryProductRepository(), new ProductLockRegistry()),
                fixedClock
        );

        orders = createOrders(NUMBER_OF_TEST_ORDERS, fixedClock);
    }

    private List<Order> createOrders(int count, Clock clock) {
        Customer customer = new Customer("CU1", "Jan", "Kowalski", "jan@test.pl");
        return IntStream.range(0, count)
                .mapToObj(i -> {
                    Electronics product = new Electronics(
                            "E" + System.nanoTime() + "-" + i, "Product " + i, new BigDecimal("10.00"), 5);
                    CartItem item = product.toCartItem(new NoConfiguration(), 1);
                    return new Order("OR" + System.nanoTime() + "-" + i, Instant.now(clock), customer,
                            List.of(item), BigDecimal.ZERO);
                })
                .toList();
    }

    @Test
    void sequentialProcessingShouldTakeAtLeastSumOfDelays() {
        // given
        OrderBatchProcessor processor = new OrderBatchProcessor(
                orderProcessor, Executors.newFixedThreadPool(1), DELAY_HOOK);

        // when
        long start = System.currentTimeMillis();
        List<Invoice> invoices = processor.processSequentially(orders);
        long elapsed = System.currentTimeMillis() - start;

        System.out.printf("[Sekwencyjnie] %d zamówień w %d ms%n", orders.size(), elapsed);

        // then
        assertThat(invoices).hasSize(orders.size());
        assertThat(elapsed).isGreaterThanOrEqualTo(orders.size() * SIMULATED_DELAY_MILLIS);
    }

    @Test
    void concurrentProcessingShouldUseMultipleThreads() throws InterruptedException {
        // given
        Set<String> threadNames = ConcurrentHashMap.newKeySet();
        ExecutorService executor = Executors.newFixedThreadPool(orders.size());
        OrderBatchProcessor processor = new OrderBatchProcessor(
                orderProcessor, executor, () -> threadNames.add(Thread.currentThread().getName()));

        // when
        List<Invoice> invoices = processor.processConcurrently(orders);
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // then
        assertThat(invoices).hasSize(orders.size());
        assertThat(threadNames.size()).isGreaterThan(1);
    }

    @Test
    void processAsyncShouldReturnImmediatelyWithoutBlockingCallingThread() {
        // given
        ExecutorService executor = Executors.newFixedThreadPool(orders.size());
        OrderBatchProcessor processor = new OrderBatchProcessor(orderProcessor, executor, DELAY_HOOK);

        // when
        long start = System.currentTimeMillis();
        CompletableFuture<List<Invoice>> future = processor.processAsync(orders);
        long callReturnedAfter = System.currentTimeMillis() - start;

        System.out.printf("[Asynchronicznie] Wywołanie processAsync wróciło po %d ms%n", callReturnedAfter);

        // then
        assertThat(callReturnedAfter).isLessThan(SIMULATED_DELAY_MILLIS);

        long joinStart = System.currentTimeMillis();
        List<Invoice> invoices = future.join();
        long totalElapsed = System.currentTimeMillis() - start;

        System.out.printf("[Asynchronicznie] %d zamówień gotowych po %d ms%n",
                invoices.size(), totalElapsed, System.currentTimeMillis() - joinStart);

        assertThat(invoices).hasSize(orders.size());

        executor.shutdown();
    }

    @Test
    void allInvoicesShouldHaveUniqueNumbersAfterConcurrentProcessing() throws InterruptedException {
        // given
        ExecutorService executor = Executors.newFixedThreadPool(orders.size());
        OrderBatchProcessor processor = new OrderBatchProcessor(orderProcessor, executor);

        // when
        List<Invoice> invoices = processor.processConcurrently(orders);
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // then
        long distinctNumbers = invoices.stream().map(Invoice::getInvoiceNumber).distinct().count();
        assertThat(distinctNumbers).isEqualTo(orders.size());
    }
}