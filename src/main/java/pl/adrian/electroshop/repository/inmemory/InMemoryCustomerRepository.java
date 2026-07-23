package pl.adrian.electroshop.repository.inmemory;

import pl.adrian.electroshop.model.customer.Customer;
import pl.adrian.electroshop.repository.CustomerRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryCustomerRepository implements CustomerRepository {

    private final Map<String, Customer> customers = new ConcurrentHashMap<>();

    @Override
    public void save(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }
        customers.put(customer.getCustomerId(), customer);
    }

    @Override
    public void deleteById(String id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        customers.remove(id);
    }

    @Override
    public Optional<Customer> findById(String id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        return Optional.ofNullable(customers.get(id));
    }

    @Override
    public List<Customer> findAll() {
        return List.copyOf(customers.values());
    }
}
