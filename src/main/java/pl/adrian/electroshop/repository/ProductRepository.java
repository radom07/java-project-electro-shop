package pl.adrian.electroshop.repository;

import pl.adrian.electroshop.model.product.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    void save(Product product);

    void deleteById(String id);

    Optional<Product> findById(String id);

    List<Product> findAll();
}
