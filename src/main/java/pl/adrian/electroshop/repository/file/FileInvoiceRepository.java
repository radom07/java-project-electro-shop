package pl.adrian.electroshop.repository.file;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import pl.adrian.electroshop.exception.FileRepositoryException;
import pl.adrian.electroshop.model.invoice.Invoice;
import pl.adrian.electroshop.repository.InvoiceRepository;
import pl.adrian.electroshop.repository.file.serialization.InvoiceFileSerializer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class FileInvoiceRepository implements InvoiceRepository {

    private final Path directory;
    private final InvoiceFileSerializer serializer;

    public FileInvoiceRepository(Path directory, InvoiceFileSerializer serializer) {
        this.directory = directory;
        this.serializer = serializer;
        try {
            Files.createDirectories(directory);
            log.debug("Invoices directory ready: {}", directory);
        } catch (IOException e) {
            log.error("Could not create invoices directory: {}", directory, e);
            throw new FileRepositoryException("Could not create invoices directory: " + directory, e);
        }
    }

    @Override
    public void save(@NonNull Invoice invoice) {
        try {
            String serializedData = serializer.serialize(invoice);
            Files.writeString(fileFor(invoice.getInvoiceNumber()), serializedData, StandardCharsets.UTF_8);
            log.debug("Invoice saved to file: {}", invoice.getInvoiceNumber());
        } catch (IOException e) {
            log.error("Could not save invoice: {}", invoice.getInvoiceNumber(), e);
            throw new FileRepositoryException("Could not save invoice: " + invoice.getInvoiceNumber(), e);
        }
    }

    @Override
    public Optional<Invoice> findByInvoiceNumber(@NonNull String invoiceNumber) {
        Path file = fileFor(invoiceNumber);
        if (!Files.exists(file)) {
            log.debug("Invoice file not found: {}", invoiceNumber);
            return Optional.empty();
        }
        try {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            return Optional.of(serializer.deserialize(lines));
        } catch (IOException e) {
            log.error("Could not read invoice file: {}", file, e);
            throw new FileRepositoryException("Could not read invoice file: " + file, e);
        }
    }

    @Override
    public List<Invoice> findAll() {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, "*.txt")) {
            List<Invoice> invoices = new ArrayList<>();
            for (Path file : stream) {
                List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
                invoices.add(serializer.deserialize(lines));
            }
            log.debug("Loaded {} invoices from {}", invoices.size(), directory);
            return List.copyOf(invoices);
        } catch (IOException e) {
            log.error("Could not list invoices in: {}", directory, e);
            throw new FileRepositoryException("Could not list invoices in: " + directory, e);
        }
    }

    private Path fileFor(String invoiceNumber) {
        return directory.resolve(invoiceNumber.replace("/", "_") + ".txt");
    }
}