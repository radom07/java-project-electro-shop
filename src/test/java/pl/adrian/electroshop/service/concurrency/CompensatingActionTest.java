package pl.adrian.electroshop.service.concurrency;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CompensatingActionTest {

    @Test
    void shouldApplyActionToAllItemsWhenNoneFail() {
        // given
        List<String> processed = new ArrayList<>();

        // when
        CompensatingAction.applyToAllOrCompensate(
                List.of("A", "B", "C"),
                processed::add,
                item -> {
                    throw new AssertionError("Compensation should not run when nothing fails");
                }
        );

        // then
        assertThat(processed).containsExactly("A", "B", "C");
    }

    @Test
    void shouldCompensateOnlyItemsProcessedBeforeFailure() {
        // given
        List<String> processed = new ArrayList<>();
        List<String> compensated = new ArrayList<>();

        // when & then
        assertThatThrownBy(() -> CompensatingAction.applyToAllOrCompensate(
                List.of("A", "B", "C"),
                item -> {
                    if (item.equals("B")) {
                        throw new IllegalStateException("failed on B");
                    }
                    processed.add(item);
                },
                compensated::add
        )).isInstanceOf(IllegalStateException.class)
                .hasMessage("failed on B");

        // then
        assertThat(processed).containsExactly("A");
        assertThat(compensated).containsExactly("A");
    }

    @Test
    void shouldNotCompensateAnythingWhenFirstItemFails() {
        // given
        List<String> compensated = new ArrayList<>();

        // when & then
        assertThatThrownBy(() -> CompensatingAction.applyToAllOrCompensate(
                List.of("A", "B"),
                item -> {
                    throw new IllegalStateException("failed immediately");
                },
                compensated::add
        )).isInstanceOf(IllegalStateException.class);

        // then
        assertThat(compensated).isEmpty();
    }

    @Test
    void shouldDoNothingForEmptyCollection() {
        // given
        List<String> processed = new ArrayList<>();

        // when
        CompensatingAction.applyToAllOrCompensate(
                List.<String>of(),
                processed::add,
                item -> {
                    throw new AssertionError("Should never be called for an empty collection");
                }
        );

        // then
        assertThat(processed).isEmpty();
    }

    @Test
    void shouldPropagateOriginalExceptionUnchanged() {
        // given
        RuntimeException original = new IllegalArgumentException("boom");

        // when & then
        assertThatThrownBy(() -> CompensatingAction.applyToAllOrCompensate(
                List.of("A"),
                item -> {
                    throw original;
                },
                item -> {
                }
        )).isSameAs(original);
    }
}