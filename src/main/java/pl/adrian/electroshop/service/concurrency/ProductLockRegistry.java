package pl.adrian.electroshop.service.concurrency;

import lombok.NonNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class ProductLockRegistry {

    private final Map<String, Object> locks = new ConcurrentHashMap<>();

    public void executeWithLock(@NonNull String productId, @NonNull Runnable action) {
        synchronized (lockFor(productId)) {
            action.run();
        }
    }

    public <T> T executeWithLock(@NonNull String productId, @NonNull Supplier<T> action) {
        synchronized (lockFor(productId)) {
            return action.get();
        }
    }

    private Object lockFor(String productId) {
        return locks.computeIfAbsent(productId, id -> new Object());
    }
}