package pl.adrian.electroshop.model.order;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class OrderStatusTest {

    @Test
    void placedShouldTransitionOnlyToPaidOrCancelled() {
        assertThat(OrderStatus.PLACED.canTransitionTo(OrderStatus.PAID)).isTrue();
        assertThat(OrderStatus.PLACED.canTransitionTo(OrderStatus.CANCELLED)).isTrue();

        assertThat(OrderStatus.PLACED.canTransitionTo(OrderStatus.SHIPPED)).isFalse();
        assertThat(OrderStatus.PLACED.canTransitionTo(OrderStatus.DELIVERED)).isFalse();
        assertThat(OrderStatus.PLACED.canTransitionTo(OrderStatus.PLACED)).isFalse();
    }

    @Test
    void paidShouldTransitionOnlyToShipped() {
        assertThat(OrderStatus.PAID.canTransitionTo(OrderStatus.SHIPPED)).isTrue();

        assertThat(OrderStatus.PAID.canTransitionTo(OrderStatus.CANCELLED)).isFalse();
        assertThat(OrderStatus.PAID.canTransitionTo(OrderStatus.DELIVERED)).isFalse();
        assertThat(OrderStatus.PAID.canTransitionTo(OrderStatus.PLACED)).isFalse();
        assertThat(OrderStatus.PAID.canTransitionTo(OrderStatus.PAID)).isFalse();
    }

    @Test
    void shippedShouldTransitionOnlyToDelivered() {
        assertThat(OrderStatus.SHIPPED.canTransitionTo(OrderStatus.DELIVERED)).isTrue();

        assertThat(OrderStatus.SHIPPED.canTransitionTo(OrderStatus.CANCELLED)).isFalse();
        assertThat(OrderStatus.SHIPPED.canTransitionTo(OrderStatus.PAID)).isFalse();
        assertThat(OrderStatus.SHIPPED.canTransitionTo(OrderStatus.PLACED)).isFalse();
        assertThat(OrderStatus.SHIPPED.canTransitionTo(OrderStatus.SHIPPED)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(OrderStatus.class)
    void deliveredShouldNeverTransitionAnywhere(OrderStatus target) {
        assertThat(OrderStatus.DELIVERED.canTransitionTo(target)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(OrderStatus.class)
    void cancelledShouldNeverTransitionAnywhere(OrderStatus target) {
        assertThat(OrderStatus.CANCELLED.canTransitionTo(target)).isFalse();
    }
}