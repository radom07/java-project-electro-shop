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

    @NonNull
    private final CustomerRepository customerRepository;

    public void addCustomer(@NonNull Customer customer) {
        if (customerRepository.findById(customer.getCustomerId()).isPresent()) {
            throw new AlreadyExistsException("Customer " + customer.getCustomerId() + " " + customer.getFirstName() + " already exists");
        }
        customerRepository.save(customer);
    }

    public void updateCustomer(@NonNull Customer customer) {
        if (customerRepository.findById(customer.getCustomerId()).isEmpty()) {
            throw new CustomerNotFoundException(customer.getCustomerId());
        }
        customerRepository.save(customer);
    }

    public void deleteCustomer(@NonNull String id) {
        customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
        customerRepository.deleteById(id);
    }

    public Optional<Customer> findCustomer(@NonNull String id) {
        return customerRepository.findById(id);
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }
}
