package pl.adrian.electroshop.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import pl.adrian.electroshop.exception.AlreadyExistsException;
import pl.adrian.electroshop.exception.InsufficientStockException;
import pl.adrian.electroshop.exception.ProductNotFoundException;
import pl.adrian.electroshop.model.product.Product;
import pl.adrian.electroshop.repository.ProductRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class ProductManager {

    @NonNull
    private final ProductRepository productRepository;

    private final Map<String, Object> productLocks = new ConcurrentHashMap<>();

    public void addProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (productRepository.findById(product.getId()).isPresent()) {
            throw new AlreadyExistsException("Product " + product.getId() + " " + product.getName() + " already exists");
        }
        productRepository.save(product);
    }

    public void updateProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (productRepository.findById(product.getId()).isEmpty()) {
            throw new ProductNotFoundException(product.getId());
        }
        productRepository.save(product);
    }

    public void removeProduct(String id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        productRepository.deleteById(id);
    }

    public Optional<Product> getProduct(String id) {
        return productRepository.findById(id);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public void reserveStock(String productId, int quantity) {
        synchronized (lockFor(productId)) {
            Product product = getProduct(productId)
                    .orElseThrow(() -> new ProductNotFoundException(productId));
            if (product.getQuantity() < quantity) {
                throw new InsufficientStockException(productId);
            }
            product.setQuantity(product.getQuantity() - quantity);
            updateProduct(product);
        }
    }

    public void releaseStock(String productId, int quantity) {
        synchronized (lockFor(productId)) {
            Product product = getProduct(productId)
                    .orElseThrow(() -> new ProductNotFoundException(productId));
            product.setQuantity(product.getQuantity() + quantity);
            updateProduct(product);
        }
    }

    private Object lockFor(String productId) {
        return productLocks.computeIfAbsent(productId, id -> new Object());
    }
}
