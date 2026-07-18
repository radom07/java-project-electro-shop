package pl.adrian.electroshop.service;

import pl.adrian.electroshop.model.product.Product;
import pl.adrian.electroshop.repository.ProductRepository;

import java.util.List;
import java.util.Optional;

public class ProductManager {

    private final ProductRepository productRepository;

    public ProductManager(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public void addProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (productRepository.findById(product.getId()).isPresent()) {
            throw new IllegalStateException("Product " + product.getId() + " " + product.getName() + " already exists");
        }
        productRepository.save(product);
    }

    public void updateProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (productRepository.findById(product.getId()).isEmpty()) {
            throw new IllegalStateException("Cannot update product. Product does not exists");
        }
        productRepository.save(product);
    }

    public void removeProduct(String id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        productRepository.deleteById(id);
    }

    public Optional<Product> getProduct(String id) {
        return productRepository.findById(id);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
}
