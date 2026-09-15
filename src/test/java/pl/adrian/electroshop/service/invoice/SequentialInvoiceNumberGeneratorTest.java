package pl.adrian.electroshop.service.invoice;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/*
TODO: test resetu licznika, wstrzyknięcie java.time.Clock do konstruktora?
 */
class SequentialInvoiceNumberGeneratorTest {

    @Test
    void shouldGenerateSequentialNumbersWithinSamePeriod() {
        // given
        SequentialInvoiceNumberGenerator generator = new SequentialInvoiceNumberGenerator();

        // when
        String first = generator.generateNumber();
        String second = generator.generateNumber();
        String third = generator.generateNumber();

        // then
        assertThat(first).endsWith("/1");
        assertThat(second).endsWith("/2");
        assertThat(third).endsWith("/3");
    }

    @Test
    void shouldFollowExpectedFormat() {
        // given
        SequentialInvoiceNumberGenerator generator = new SequentialInvoiceNumberGenerator();

        // when
        String number = generator.generateNumber();

        // then
        assertThat(number).matches("FV/\\d{4}/\\d{2}/\\d+");
    }

    @Test
    void shouldNeverGenerateDuplicateNumbersUnderConcurrentAccess() throws InterruptedException {
        // given
        SequentialInvoiceNumberGenerator generator = new SequentialInvoiceNumberGenerator();
        int threadCount = 20;
        int numbersPerThread = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        Set<String> generatedNumbers = java.util.Collections.synchronizedSet(new HashSet<>());

        // when
        IntStream.range(0, threadCount).forEach(i ->
                executor.submit(() -> {
                    for (int j = 0; j < numbersPerThread; j++) {
                        generatedNumbers.add(generator.generateNumber());
                    }
                })
        );
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // then
        assertThat(generatedNumbers).hasSize(threadCount * numbersPerThread);
    }
}