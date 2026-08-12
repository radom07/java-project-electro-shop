package pl.adrian.electroshop.service.invoice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RequiredArgsConstructor
public class SequentialInvoiceNumberGenerator implements InvoiceNumberGenerator {

    private final Map<String, AtomicInteger> countersByYearMonth = new ConcurrentHashMap<>();

    private final Clock clock;

    @Override
    public String generateNumber() {
        LocalDate today = LocalDate.now(clock);
        int year = today.getYear();
        int month = today.getMonthValue();
        String periodKey = year + "-" + month;

        int sequenceNumber = countersByYearMonth
                .computeIfAbsent(periodKey, key -> new AtomicInteger(0))
                .incrementAndGet();

        String invoiceNumber = String.format("FV/%d/%02d/%d", year, month, sequenceNumber);
        log.debug("Generated invoice number: {}", invoiceNumber);
        return invoiceNumber;
    }
}
