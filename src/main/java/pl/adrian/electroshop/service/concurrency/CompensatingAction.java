package pl.adrian.electroshop.service.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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
            for (T item : processed) {
                compensation.accept(item);
            }
            throw e;
        }
    }
}