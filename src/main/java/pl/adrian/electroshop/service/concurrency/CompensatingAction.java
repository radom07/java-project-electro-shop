package pl.adrian.electroshop.service.concurrency;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
public final class CompensatingAction {

    private CompensatingAction() {
    }

    public static <T> void applyToAllOrCompensate(Iterable<T> items, Consumer<T> action, Consumer<T> compensation) {
        List<T> processed = new ArrayList<>();
        try {
            for (T item : items) {
                action.accept(item);
                processed.add(item);
            }
        } catch (RuntimeException e) {
            log.warn("Action failed after processing {} item(s), rolling back via compensation", processed.size(), e);
            for (T item : processed) {
                try {
                    compensation.accept(item);
                } catch (RuntimeException compensationException) {
                    log.error("Compensation failed for item {}", item, compensationException);
                    e.addSuppressed(compensationException);
                }
            }
            throw e;
        }
    }
}