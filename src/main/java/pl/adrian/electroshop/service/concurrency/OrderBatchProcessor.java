package pl.adrian.electroshop.service.concurrency;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.adrian.electroshop.model.invoice.Invoice;
import pl.adrian.electroshop.model.order.Order;
import pl.adrian.electroshop.service.OrderProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Slf4j
@RequiredArgsConstructor
public class OrderBatchProcessor {

    @NonNull
    private final OrderProcessor orderProcessor;
    @NonNull
    private final ExecutorService executorService;
    private final long simulatedDelayMillis;

    public List<Invoice> processSequentially(List<Order> orders) {
        log.info("Processing {} order(s) sequentially", orders.size());
        List<Invoice> invoices = new ArrayList<>();
        for (Order order : orders) {
            invoices.add(processOrderWithDelay(order));
        }
        return invoices;
    }

    public List<Invoice> processConcurrently(List<Order> orders) {
        log.info("Processing {} order(s) concurrently", orders.size());
        List<Future<Invoice>> futures = orders.stream()
                .map(order -> executorService.submit(() -> processOrderWithDelay(order)))
                .toList();

        List<Invoice> invoices = new ArrayList<>();
        for (Future<Invoice> future : futures) {
            invoices.add(resolve(future));
        }
        return invoices;
    }

    public CompletableFuture<List<Invoice>> processAsync(List<Order> orders) {
        log.info("Processing {} order(s) asynchronously", orders.size());
        List<CompletableFuture<Invoice>> futures = orders.stream()
                .map(order -> CompletableFuture.supplyAsync(
                        () -> processOrderWithDelay(order), executorService))
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream().map(CompletableFuture::join).toList());
    }

    private Invoice processOrderWithDelay(Order order) {
        simulateExternalProcessingDelay();
        return orderProcessor.processOrder(order);
    }

    private void simulateExternalProcessingDelay() {
        if (simulatedDelayMillis <= 0) {
            return;
        }
        try {
            Thread.sleep(simulatedDelayMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Order processing was interrupted during simulated delay", e);
            throw new IllegalStateException("Order processing was interrupted", e);
        }
    }

    private Invoice resolve(Future<Invoice> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Order processing was interrupted while waiting for result", e);
            throw new IllegalStateException("Order processing was interrupted", e);
        } catch (ExecutionException e) {
            log.error("Order processing failed", e.getCause());
            throw new IllegalStateException("Order processing failed", e.getCause());
        }
    }
}