package pl.adrian.electroshop.repository;

import pl.adrian.electroshop.model.customer.Customer;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository {
    void save(Customer customer);

    void deleteById(String id);

    Optional<Customer> findById(String id);

    List<Customer> findAll();
}
