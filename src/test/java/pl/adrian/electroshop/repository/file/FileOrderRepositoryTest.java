package pl.adrian.electroshop.repository.file;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.adrian.electroshop.exception.FileRepositoryException;
import pl.adrian.electroshop.model.customer.Customer;
import pl.adrian.electroshop.model.order.Order;
import pl.adrian.electroshop.model.product.CartItem;
import pl.adrian.electroshop.model.product.Electronics;
import pl.adrian.electroshop.model.product.configuration.NoConfiguration;
import pl.adrian.electroshop.repository.file.serialization.OrderFileSerializer;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileOrderRepositoryTest {

    @TempDir
    Path tempDir; // Katalog tymczasowy, niszczony po teście

    @Mock
    private OrderFileSerializer serializer;

    private FileOrderRepository repository;
    private Order sampleOrder;

    @BeforeEach
    void setUp() {
        repository = new FileOrderRepository(tempDir, serializer);

        Customer customer = new Customer("CU1", "Jan", "Kowalski", "jan.kowalski@test.pl");
        Electronics cable = new Electronics("E1", "USB-C Cable", new BigDecimal("49.99"), 100);
        CartItem cartItem = cable.toCartItem(new NoConfiguration(), 2);

        sampleOrder = new Order("OR1", Instant.now(), customer, List.of(cartItem), new BigDecimal("99.98"));
    }

    @Test
    void shouldSaveOrderToFile() {
        // given
        when(serializer.serialize(sampleOrder)).thenReturn("mocked=data\n");

        // when
        repository.save(sampleOrder);

        // then
        Path expectedFile = tempDir.resolve("OR1.txt");
        assertThat(Files.exists(expectedFile)).isTrue();
    }

    @Test
    void shouldFindOrderById() {
        // given
        when(serializer.serialize(sampleOrder)).thenReturn("mocked=data");
        repository.save(sampleOrder);

        when(serializer.deserialize(any())).thenReturn(sampleOrder);

        // when
        Optional<Order> found = repository.findById("OR1");

        // then
        assertThat(found).isPresent().contains(sampleOrder);
    }

    @Test
    void shouldReturnEmptyWhenOrderDoesNotExist() {
        // when
        Optional<Order> found = repository.findById("MISSING");

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void shouldDeleteOrderById() {
        // given
        when(serializer.serialize(sampleOrder)).thenReturn("mocked=data");
        repository.save(sampleOrder);
        assertThat(Files.exists(tempDir.resolve("OR1.txt"))).isTrue();

        // when
        repository.deleteById("OR1");

        // then
        assertThat(Files.exists(tempDir.resolve("OR1.txt"))).isFalse();
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenSavingNullOrder() {
        // when & then
        assertThatThrownBy(() -> repository.save(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Order cannot be null");
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenFindingWithNullId() {
        // when & then
        assertThatThrownBy(() -> repository.findById(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID cannot be null");
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenDeletingWithNullId() {
        // when & then
        assertThatThrownBy(() -> repository.deleteById(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID cannot be null");
    }

    @Test
    void shouldThrowFileRepositoryExceptionWhenSavingFailsDueToIoError() {
        // given
        when(serializer.serialize(sampleOrder)).thenReturn("mocked=data");

        tempDir.toFile().delete();

        // when & then
        assertThatThrownBy(() -> repository.save(sampleOrder))
                .isInstanceOf(FileRepositoryException.class)
                .hasMessageContaining("Could not save order: OR1")
                .hasCauseInstanceOf(java.io.IOException.class);
    }

    @Test
    void shouldThrowFileRepositoryExceptionWhenListingAllFailsDueToIoError() {
        tempDir.toFile().delete();

        // when & then
        assertThatThrownBy(() -> repository.findAll())
                .isInstanceOf(FileRepositoryException.class)
                .hasMessageContaining("Could not list orders")
                .hasCauseInstanceOf(java.io.IOException.class);
    }
}