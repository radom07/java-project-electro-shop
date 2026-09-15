package pl.adrian.electroshop.repository;

import pl.adrian.electroshop.model.order.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    void save(Order order);

    void deleteById(String id);

    Optional<Order> findById(String id);

    List<Order> findAll();
}
