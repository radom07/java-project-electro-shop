package pl.adrian.electroshop.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import pl.adrian.electroshop.model.invoice.Invoice;
import pl.adrian.electroshop.model.order.Order;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@RequiredArgsConstructor
public class OrderBatchProcessor {

    @NonNull private final OrderProcessor orderProcessor;
    @NonNull private final ExecutorService executorService;
    private final long simulatedDelayMillis;

    public List<Invoice> processSequentially(List<Order> orders) {
        List<Invoice> invoices = new ArrayList<>();
        for (Order order : orders) {
            invoices.add(processWithSimulatedDelay(order));
        }
        return invoices;
    }

    public List<Invoice> processConcurrently(List<Order> orders) {
        List<Future<Invoice>> futures = orders.stream()
                .map(order -> executorService.submit(() -> processWithSimulatedDelay(order)))
                .toList();

        List<Invoice> invoices = new ArrayList<>();
        for (Future<Invoice> future : futures) {
            invoices.add(resolve(future));
        }
        return invoices;
    }

    public CompletableFuture<List<Invoice>> processAsync(List<Order> orders) {
        List<CompletableFuture<Invoice>> futures = orders.stream()
                .map(order -> CompletableFuture.supplyAsync(
                        () -> processWithSimulatedDelay(order), executorService))
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream().map(CompletableFuture::join).toList());
    }

    private Invoice processWithSimulatedDelay(Order order) {
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
            throw new IllegalStateException("Order processing was interrupted", e);
        }
    }

    private Invoice resolve(Future<Invoice> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Order processing was interrupted", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Order processing failed", e.getCause());
        }
    }
}