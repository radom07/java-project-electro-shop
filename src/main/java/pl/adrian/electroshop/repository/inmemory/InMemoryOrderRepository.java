package pl.adrian.electroshop.repository.inmemory;

import lombok.NonNull;
import pl.adrian.electroshop.model.order.Order;
import pl.adrian.electroshop.repository.OrderRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryOrderRepository implements OrderRepository {

    private final Map<String, Order> orders = new ConcurrentHashMap<>();

    @Override
    public void save(@NonNull Order order) {
        orders.put(order.getOrderId(), order);
    }

    @Override
    public void deleteById(@NonNull String id) {
        orders.remove(id);
    }

    @Override
    public Optional<Order> findById(@NonNull String id) {
        return Optional.ofNullable(orders.get(id));
    }

    @Override
    public List<Order> findAll() {
        return List.copyOf(orders.values());
    }
}
