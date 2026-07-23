package pl.adrian.electroshop.repository.inmemory;

import pl.adrian.electroshop.model.invoice.Invoice;
import pl.adrian.electroshop.repository.InvoiceRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryInvoiceRepository implements InvoiceRepository {

    private final Map<String, Invoice> invoices = new ConcurrentHashMap<>();

    @Override
    public void save(Invoice invoice) {
        if (invoice == null) {
            throw new IllegalArgumentException("Invoice cannot be null");
        }
        invoices.put(invoice.getInvoiceNumber(), invoice);
    }

    @Override
    public Optional<Invoice> findByInvoiceNumber(String invoiceNumber) {
        if (invoiceNumber == null) {
            throw new IllegalArgumentException("Invoice number cannot be null");
        }
        return Optional.ofNullable(invoices.get(invoiceNumber));
    }

    @Override
    public List<Invoice> findAll() {
        return List.copyOf(invoices.values());
    }
}
