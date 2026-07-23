package pl.adrian.electroshop.repository;

import pl.adrian.electroshop.model.invoice.Invoice;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository {
    void save(Invoice invoice);

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    List<Invoice> findAll();
}
