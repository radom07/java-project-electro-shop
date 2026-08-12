package pl.adrian.electroshop.service.concurrency;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class ProductLockRegistryTest {

    @Test
    void shouldExecuteRunnableWithLock() {
        // given
        ProductLockRegistry registry = new ProductLockRegistry();
        boolean[] executed = {false};

        // when
        registry.executeWithLock("P1", () -> executed[0] = true);

        // then
        assertThat(executed[0]).isTrue();
    }

    @Test
    void shouldExecuteSupplierWithLockAndReturnValue() {
        // given
        ProductLockRegistry registry = new ProductLockRegistry();

        // when
        String result = registry.executeWithLock("P1", () -> "Success");

        // then
        assertThat(result).isEqualTo("Success");
    }

    @Test
    void shouldSerializeAccessForSameProductId() throws InterruptedException {
        // given
        ProductLockRegistry registry = new ProductLockRegistry();
        int numberOfThreads = 100;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        int[] unsafeCounter = {0};

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(numberOfThreads);

        // when
        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();

                    registry.executeWithLock("PROD-1", () -> {
                        int current = unsafeCounter[0];
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException ignored) {
                        }
                        unsafeCounter[0] = current + 1;
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        // then
        assertThat(unsafeCounter[0]).isEqualTo(numberOfThreads);
    }
}