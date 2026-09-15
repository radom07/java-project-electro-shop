package pl.adrian.electroshop.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import pl.adrian.electroshop.exception.AlreadyExistsException;
import pl.adrian.electroshop.exception.CustomerNotFoundException;
import pl.adrian.electroshop.model.customer.Customer;
import pl.adrian.electroshop.repository.CustomerRepository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class CustomerManager {

    @NonNull private final CustomerRepository customerRepository;

    public void addCustomer(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }
        if (customerRepository.findById(customer.getCustomerId()).isPresent()) {
            throw new AlreadyExistsException("Customer " + customer.getCustomerId() + " " + customer.getFirstName() + " already exists");
        }
        customerRepository.save(customer);
    }

    public void updateCustomer(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }
        if (customerRepository.findById(customer.getCustomerId()).isEmpty()) {
            throw new CustomerNotFoundException(customer.getCustomerId());
        }
        customerRepository.save(customer);
    }

    public void deleteCustomer(String id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
        customerRepository.deleteById(id);
    }

    public Optional<Customer> getCustomer(String id) {
        return customerRepository.findById(id);
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }
}
