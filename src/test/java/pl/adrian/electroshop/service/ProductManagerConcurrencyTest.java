package pl.adrian.electroshop.service;

import org.junit.jupiter.api.Test;
import pl.adrian.electroshop.exception.InsufficientStockException;
import pl.adrian.electroshop.model.product.Electronics;
import pl.adrian.electroshop.repository.inmemory.InMemoryProductRepository;
import pl.adrian.electroshop.service.concurrency.ProductLockRegistry;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class ProductManagerConcurrencyTest {

    @Test
    void shouldNeverOversellStockUnderConcurrentReservations() throws InterruptedException {
        // given
        ProductManager productManager = new ProductManager(new InMemoryProductRepository(), new ProductLockRegistry());

        int initialStock = 10;
        productManager.addProduct(new Electronics("E1", "Limited Item", new BigDecimal("99.99"), initialStock));

        int attemptCount = 50;
        ExecutorService executor = Executors.newFixedThreadPool(attemptCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();

        // when
        for (int i = 0; i < attemptCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    productManager.reserveStock("E1", 1);
                    successCount.incrementAndGet();
                } catch (InsufficientStockException e) {
                    failureCount.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        startLatch.countDown();
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // then
        assertThat(successCount.get()).isEqualTo(initialStock);
        assertThat(failureCount.get()).isEqualTo(attemptCount - initialStock);
        assertThat(productManager.findProduct("E1").orElseThrow().getQuantity()).isZero();
    }

    @Test
    void shouldAllowConcurrentOperationsOnDifferentProductsWithoutInterference() throws InterruptedException {
        // given
        ProductManager productManager = new ProductManager(new InMemoryProductRepository(), new ProductLockRegistry());
        productManager.addProduct(new Electronics("E1", "Item A", new BigDecimal("10.00"), 100));
        productManager.addProduct(new Electronics("E2", "Item B", new BigDecimal("20.00"), 100));

        ExecutorService executor = Executors.newFixedThreadPool(2);

        // when
        executor.submit(() -> {
            for (int i = 0; i < 50; i++) {
                productManager.reserveStock("E1", 1);
            }
        });
        executor.submit(() -> {
            for (int i = 0; i < 50; i++) {
                productManager.reserveStock("E2", 1);
            }
        });
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // then
        assertThat(productManager.findProduct("E1").orElseThrow().getQuantity()).isEqualTo(50);
        assertThat(productManager.findProduct("E2").orElseThrow().getQuantity()).isEqualTo(50);
    }
}