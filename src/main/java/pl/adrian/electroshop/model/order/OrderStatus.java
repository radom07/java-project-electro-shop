package pl.adrian.electroshop.model.order;

import java.util.Set;

public enum OrderStatus {
    PLACED,
    PAID,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    private static final Set<OrderStatus> FINAL_STATUSES = Set.of(DELIVERED, CANCELLED);

    public boolean canTransitionTo(OrderStatus newStatus) {
        if (FINAL_STATUSES.contains(this)) {
            return false;
        }
        return switch (this) {
            case PLACED -> newStatus == PAID || newStatus == CANCELLED;
            case PAID -> newStatus == SHIPPED;
            case SHIPPED -> newStatus == DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }
}
