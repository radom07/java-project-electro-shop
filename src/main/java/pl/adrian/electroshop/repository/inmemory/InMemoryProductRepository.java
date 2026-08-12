package pl.adrian.electroshop.repository.inmemory;

import lombok.NonNull;
import pl.adrian.electroshop.model.product.Product;
import pl.adrian.electroshop.repository.ProductRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryProductRepository implements ProductRepository {

    private final Map<String, Product> products = new ConcurrentHashMap<>();

    @Override
    public void save(@NonNull Product product) {
        products.put(product.getId(), product);
    }

    @Override
    public void deleteById(@NonNull String id) {
        products.remove(id);
    }

    @Override
    public Optional<Product> findById(@NonNull String id) {
        return Optional.ofNullable(products.get(id));
    }

    @Override
    public List<Product> findAll() {
        return List.copyOf(products.values());
    }
}