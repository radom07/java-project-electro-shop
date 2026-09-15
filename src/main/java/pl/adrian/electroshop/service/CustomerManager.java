package pl.adrian.electroshop.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.adrian.electroshop.exception.AlreadyExistsException;
import pl.adrian.electroshop.exception.CustomerNotFoundException;
import pl.adrian.electroshop.model.customer.Customer;
import pl.adrian.electroshop.repository.CustomerRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class CustomerManager {

    @NonNull
    private final CustomerRepository customerRepository;

    public void addCustomer(@NonNull Customer customer) {
        if (customerRepository.findById(customer.getCustomerId()).isPresent()) {
            log.warn("Attempted to add already existing customer: {}", customer.getCustomerId());
            throw new AlreadyExistsException("Customer " + customer.getCustomerId() + " " + customer.getFirstName() + " already exists");
        }
        customerRepository.save(customer);
        log.info("Customer added: {}", customer.getCustomerId());
    }

    public void updateCustomer(@NonNull Customer customer) {
        if (customerRepository.findById(customer.getCustomerId()).isEmpty()) {
            throw new CustomerNotFoundException(customer.getCustomerId());
        }
        customerRepository.save(customer);
        log.debug("Customer updated: {}", customer.getCustomerId());
    }

    public void deleteCustomer(@NonNull String id) {
        customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
        customerRepository.deleteById(id);
        log.info("Customer deleted: {}", id);
    }

    public Optional<Customer> findCustomer(@NonNull String id) {
        return customerRepository.findById(id);
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }
}
