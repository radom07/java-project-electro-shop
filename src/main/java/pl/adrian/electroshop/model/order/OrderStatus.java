package pl.adrian.electroshop.model.order;

import java.util.Set;

/*
Design Pattern: STATE (behavioral, GoF)
Instead of keeping the status as a plain string/enum and checking allowed
transitions using if/else statements scattered across services, the state itself
knows which other states it is allowed to transition to

As a result, OrderProcessor#changeOrderStatus and Order#changeStatus
do not contain a single business rule regarding the order lifecycle - the entire
"state machine" lives in one place and is easy to test in isolation

Terminal states DELIVERED, CANCELLED do not allow any
further transitions - this is also part of the state contract, not caller logic
 */
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