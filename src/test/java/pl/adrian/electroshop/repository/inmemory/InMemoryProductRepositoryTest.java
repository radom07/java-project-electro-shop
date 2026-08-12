package pl.adrian.electroshop.repository.inmemory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.adrian.electroshop.model.product.Electronics;
import pl.adrian.electroshop.model.product.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemoryProductRepositoryTest {

    private InMemoryProductRepository repository;
    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        repository = new InMemoryProductRepository();
        sampleProduct = new Electronics("E1", "Charger", new BigDecimal("99.99"), 5);
    }

    @Test
    void shouldSaveAndFindProduct() {
        // when
        repository.save(sampleProduct);
        Optional<Product> found = repository.findById("E1");

        // then
        assertThat(found).isPresent().contains(sampleProduct);
    }

    @Test
    void shouldOverwriteProductWhenSavingWithSameId() {
        // given
        repository.save(sampleProduct);
        Product updatedProduct = new Electronics("E1", "Updated Charger", new BigDecimal("120.00"), 10);

        // when
        repository.save(updatedProduct);
        Optional<Product> found = repository.findById("E1");

        // then
        assertThat(found).isPresent().contains(updatedProduct);
        assertThat(found.get().getName()).isEqualTo("Updated Charger");
    }

    @Test
    void shouldThrowExceptionWhenSavingNullProduct() {
        // when & then
        assertThatThrownBy(() -> repository.save(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("is marked non-null but is null");
    }

    @Test
    void shouldDeleteProductById() {
        // given
        repository.save(sampleProduct);

        // when
        repository.deleteById("E1");
        Optional<Product> found = repository.findById("E1");

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void shouldThrowExceptionWhenDeletingWithNullId() {
        // when & then
        assertThatThrownBy(() -> repository.deleteById(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("is marked non-null but is null");
    }

    @Test
    void shouldThrowExceptionWhenFindingWithNullId() {
        // when & then
        assertThatThrownBy(() -> repository.findById(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("is marked non-null but is null");
    }

    @Test
    void shouldReturnAllSavedProducts() {
        // given
        Product secondProduct = new Electronics("E2", "USB Cable", new BigDecimal("19.99"), 10);
        repository.save(sampleProduct);
        repository.save(secondProduct);

        // when
        List<Product> all = repository.findAll();

        // then
        assertThat(all).hasSize(2).contains(sampleProduct, secondProduct);
    }

    @Test
    void shouldReturnUnmodifiableListFromFindAll() {
        // given
        repository.save(sampleProduct);
        List<Product> all = repository.findAll();

        // when & then
        Product anotherProduct = new Electronics("E2", "Cable", new BigDecimal("10.00"), 1);
        assertThatThrownBy(() -> all.add(anotherProduct))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}