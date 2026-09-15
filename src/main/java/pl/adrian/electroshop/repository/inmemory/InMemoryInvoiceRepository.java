package pl.adrian.electroshop.repository.inmemory;

import lombok.NonNull;
import pl.adrian.electroshop.model.invoice.Invoice;
import pl.adrian.electroshop.repository.InvoiceRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryInvoiceRepository implements InvoiceRepository {

    private final Map<String, Invoice> invoices = new ConcurrentHashMap<>();

    @Override
    public void save(@NonNull Invoice invoice) {
        invoices.put(invoice.getInvoiceNumber(), invoice);
    }

    @Override
    public Optional<Invoice> findByInvoiceNumber(@NonNull String invoiceNumber) {
        return Optional.ofNullable(invoices.get(invoiceNumber));
    }

    @Override
    public List<Invoice> findAll() {
        return List.copyOf(invoices.values());
    }
}