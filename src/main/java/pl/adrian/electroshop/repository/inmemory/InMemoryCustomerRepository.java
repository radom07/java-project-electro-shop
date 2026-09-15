package pl.adrian.electroshop.repository.inmemory;

import lombok.NonNull;
import pl.adrian.electroshop.model.customer.Customer;
import pl.adrian.electroshop.repository.CustomerRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryCustomerRepository implements CustomerRepository {

    private final Map<String, Customer> customers = new ConcurrentHashMap<>();

    @Override
    public void save(@NonNull Customer customer) {
        customers.put(customer.getCustomerId(), customer);
    }

    @Override
    public void deleteById(@NonNull String id) {
        customers.remove(id);
    }

    @Override
    public Optional<Customer> findById(@NonNull String id) {
        return Optional.ofNullable(customers.get(id));
    }

    @Override
    public List<Customer> findAll() {
        return List.copyOf(customers.values());
    }
}