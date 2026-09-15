package pl.adrian.electroshop.service.invoice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class SequentialInvoiceNumberGeneratorTest {

    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.parse("2026-08-04T10:00:00Z"), ZoneId.of("Europe/Warsaw"));
    }

    @Test
    void shouldGenerateSequentialNumbersWithinSamePeriod() {
        // given
        SequentialInvoiceNumberGenerator generator = new SequentialInvoiceNumberGenerator(fixedClock);

        // when
        String first = generator.generateNumber();
        String second = generator.generateNumber();

        // then
        assertThat(first).endsWith("/1");
        assertThat(first).contains("/2026/08/");
        assertThat(second).endsWith("/2");
    }

    @Test
    void shouldFollowExpectedFormat() {
        // given
        SequentialInvoiceNumberGenerator generator = new SequentialInvoiceNumberGenerator(fixedClock);

        // when
        String number = generator.generateNumber();

        // then
        assertThat(number).matches("FV/\\d{4}/\\d{2}/\\d+");
    }

    @Test
    void shouldNeverGenerateDuplicateNumbersUnderConcurrentAccess() throws InterruptedException {
        // given
        SequentialInvoiceNumberGenerator generator = new SequentialInvoiceNumberGenerator(fixedClock);
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