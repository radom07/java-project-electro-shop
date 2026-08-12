package pl.adrian.electroshop.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import pl.adrian.electroshop.exception.AlreadyExistsException;
import pl.adrian.electroshop.exception.ProductNotFoundException;
import pl.adrian.electroshop.model.product.Product;
import pl.adrian.electroshop.repository.ProductRepository;
import pl.adrian.electroshop.service.concurrency.ProductLockRegistry;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class ProductManager {

    @NonNull
    private final ProductRepository productRepository;
    @NonNull
    private final ProductLockRegistry lockRegistry;

    public void addProduct(@NonNull Product product) {
        if (productRepository.findById(product.getId()).isPresent()) {
            throw new AlreadyExistsException("Product " + product.getId() + " " + product.getName() + " already exists");
        }
        productRepository.save(product);
    }

    public void updateProduct(@NonNull Product product) {
        if (productRepository.findById(product.getId()).isEmpty()) {
            throw new ProductNotFoundException(product.getId());
        }
        productRepository.save(product);
    }

    public void removeProduct(@NonNull String id) {
        productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        productRepository.deleteById(id);
    }

    public Optional<Product> findProduct(@NonNull String id) {
        return productRepository.findById(id);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public void reserveStock(@NonNull String productId, int quantity) {
        lockRegistry.executeWithLock(productId, () -> {
            Product product = findProduct(productId)
                    .orElseThrow(() -> new ProductNotFoundException(productId));
            product.decreaseStock(quantity);
            updateProduct(product);
        });
    }

    public void releaseStock(@NonNull String productId, int quantity) {
        lockRegistry.executeWithLock(productId, () -> {
            Product product = findProduct(productId)
                    .orElseThrow(() -> new ProductNotFoundException(productId));
            product.increaseStock(quantity);
            updateProduct(product);
        });
    }
}