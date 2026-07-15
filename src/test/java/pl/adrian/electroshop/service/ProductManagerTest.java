package pl.adrian.electroshop.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.adrian.electroshop.model.Electronics;
import pl.adrian.electroshop.model.Product;
import pl.adrian.electroshop.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductManagerTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductManager productManager;

    private Product sampleProduct;
    private String productId;

    @BeforeEach
    void setUp() {
        productId = "E1";
        sampleProduct = new Electronics(productId, "Charger", new BigDecimal("99.99"), 5);
    }

    @Test
    void shouldAddProductSuccessfullyWhenProductDoesNotExist() {
        // given
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // when
        productManager.addProduct(sampleProduct);

        // then
        verify(productRepository, times(1)).save(sampleProduct);
    }

    @Test
    void shouldThrowExceptionWhenAddingProductThatAlreadyExists() {
        // given
        when(productRepository.findById(productId)).thenReturn(Optional.of(sampleProduct));

        // when & then
        assertThatThrownBy(() -> productManager.addProduct(sampleProduct))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already exists");

        verify(productRepository, never()).save(any());
    }

    @Test
    void shouldUpdateProductSuccessfullyWhenProductExists() {
        // given
        when(productRepository.findById(productId)).thenReturn(Optional.of(sampleProduct));

        // when
        productManager.updateProduct(sampleProduct);

        // then
        verify(productRepository, times(1)).save(sampleProduct);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentProduct() {
        // given
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productManager.updateProduct(sampleProduct))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot update product");

        verify(productRepository, never()).save(any());
    }

    @Test
    void shouldRemoveProductSuccessfullyWhenProductExists() {
        // given
        when(productRepository.findById(productId)).thenReturn(Optional.of(sampleProduct));

        // when
        productManager.removeProduct(productId);

        // then
        verify(productRepository, times(1)).deleteById(productId);
    }

    @Test
    void shouldThrowExceptionWhenRemovingNonExistentProduct() {
        // given
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productManager.removeProduct(productId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product not found");

        verify(productRepository, never()).deleteById(anyString());
    }

    @Test
    void shouldReturnProductWhenItExistsInRepository() {
        // given
        when(productRepository.findById(productId)).thenReturn(Optional.of(sampleProduct));

        // when
        Optional<Product> result = productManager.getProduct(productId);

        // then
        assertThat(result).isPresent().contains(sampleProduct);
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    void shouldReturnEmptyOptionalWhenProductDoesNotExist() {
        // given
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // when
        Optional<Product> result = productManager.getProduct(productId);

        // then
        assertThat(result).isEmpty();
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    void shouldReturnAllProductsFromRepository() {
        // given
        List<Product> mockList = List.of(
                sampleProduct,
                new Electronics("E2", "USB Cable", new BigDecimal("19.99"), 10)
        );
        when(productRepository.findAll()).thenReturn(mockList);

        // when
        List<Product> result = productManager.getAllProducts();

        // then
        assertThat(result)
                .hasSize(2)
                .containsExactlyElementsOf(mockList);
        verify(productRepository, times(1)).findAll();
    }
}