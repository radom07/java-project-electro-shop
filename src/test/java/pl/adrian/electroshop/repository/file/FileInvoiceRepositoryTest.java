package pl.adrian.electroshop.repository.file;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.adrian.electroshop.model.customer.Customer;
import pl.adrian.electroshop.model.invoice.Invoice;
import pl.adrian.electroshop.model.order.Order;
import pl.adrian.electroshop.model.product.CartItem;
import pl.adrian.electroshop.model.product.Electronics;
import pl.adrian.electroshop.model.product.configuration.NoConfiguration;
import pl.adrian.electroshop.repository.file.serialization.InvoiceFileSerializer;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileInvoiceRepositoryTest {

    @TempDir
    Path tempDir;

    @Mock
    private InvoiceFileSerializer serializer;

    private FileInvoiceRepository repository;
    private Invoice sampleInvoice;

    @BeforeEach
    void setUp() {
        repository = new FileInvoiceRepository(tempDir, serializer);

        Customer customer = new Customer("CU1", "Jan", "Kowalski", "jan.kowalski@test.pl");
        Electronics cable = new Electronics("E1", "USB-C Cable", new BigDecimal("49.99"), 100);
        CartItem cartItem = cable.toCartItem(new NoConfiguration(), 2);

        Order order = new Order("OR1", Instant.now(), customer, List.of(cartItem), new BigDecimal("99.98"), BigDecimal.ZERO);
        sampleInvoice = new Invoice("FV/2026/08/1", LocalDate.now(), order);
    }

    @Test
    void shouldSaveInvoiceToFileReplacingSlashesWithUnderscores() {
        // given
        when(serializer.serialize(sampleInvoice)).thenReturn("invoice=data");

        // when
        repository.save(sampleInvoice);

        // then
        Path expectedFile = tempDir.resolve("FV_2026_08_1.txt"); // "/" zamienione na "_"
        assertThat(Files.exists(expectedFile)).isTrue();
    }

    @Test
    void shouldFindInvoiceByInvoiceNumber() {
        // given
        when(serializer.serialize(sampleInvoice)).thenReturn("invoice=data");
        repository.save(sampleInvoice);

        when(serializer.deserialize(any())).thenReturn(sampleInvoice);

        // when
        Optional<Invoice> found = repository.findByInvoiceNumber("FV/2026/08/1");

        // then
        assertThat(found).isPresent().contains(sampleInvoice);
    }

    @Test
    void shouldReturnEmptyWhenInvoiceDoesNotExist() {
        // when
        Optional<Invoice> found = repository.findByInvoiceNumber("MISSING/1");

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void shouldThrowNullPointerExceptionWhenSavingNullInvoice() {
        // when & then
        assertThatThrownBy(() -> repository.save(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("is marked non-null but is null");
    }

    @Test
    void shouldThrowNullPointerExceptionWhenFindingWithNullInvoiceNumber() {
        // when & then
        assertThatThrownBy(() -> repository.findByInvoiceNumber(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("is marked non-null but is null");
    }

    @Test
    void shouldThrowFileRepositoryExceptionWhenSavingFailsDueToIoError() {
        // given
        when(serializer.serialize(sampleInvoice)).thenReturn("invoice=data");

        tempDir.toFile().delete();

        // when & then
        assertThatThrownBy(() -> repository.save(sampleInvoice))
                .isInstanceOf(pl.adrian.electroshop.exception.FileRepositoryException.class)
                .hasMessageContaining("Could not save invoice: FV/2026/08/1")
                .hasCauseInstanceOf(java.io.IOException.class);
    }

    @Test
    void shouldThrowFileRepositoryExceptionWhenListingAllFailsDueToIoError() {
        tempDir.toFile().delete();

        // when & then
        assertThatThrownBy(() -> repository.findAll())
                .isInstanceOf(pl.adrian.electroshop.exception.FileRepositoryException.class)
                .hasMessageContaining("Could not list invoices")
                .hasCauseInstanceOf(java.io.IOException.class);
    }
}