package pl.adrian.electroshop.repository.inmemory;

import pl.adrian.electroshop.model.order.Order;
import pl.adrian.electroshop.repository.OrderRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryOrderRepository implements OrderRepository {

    private final Map<String, Order> orders = new ConcurrentHashMap<>();

    @Override
    public void save(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        orders.put(order.getOrderId(), order);
    }

    @Override
    public void deleteById(String id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        orders.remove(id);
    }

    @Override
    public Optional<Order> findById(String id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        return Optional.ofNullable(orders.get(id));
    }

    @Override
    public List<Order> findAll() {
        return List.copyOf(orders.values());
    }
}
