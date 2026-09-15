package pl.adrian.electroshop.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.adrian.electroshop.exception.AlreadyExistsException;
import pl.adrian.electroshop.exception.CustomerNotFoundException;
import pl.adrian.electroshop.model.customer.Customer;
import pl.adrian.electroshop.repository.CustomerRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerManagerTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerManager customerManager;

    private Customer sampleCustomer;
    private String customerId;

    @BeforeEach
    void setUp() {
        customerId = "CU1";
        sampleCustomer = new Customer(customerId, "Jan", "Kowalski", "jan.kowalski@example.com");
    }

    @Test
    void shouldAddCustomerSuccessfullyWhenCustomerDoesNotExist() {
        // given
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // when
        customerManager.addCustomer(sampleCustomer);

        // then
        verify(customerRepository, times(1)).save(sampleCustomer);
    }

    @Test
    void shouldThrowExceptionWhenAddingNullCustomer() {
        // when & then
        assertThatThrownBy(() -> customerManager.addCustomer(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("is marked non-null but is null");

        verify(customerRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenAddingCustomerThatAlreadyExists() {
        // given
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(sampleCustomer));

        // when & then
        assertThatThrownBy(() -> customerManager.addCustomer(sampleCustomer))
                .isInstanceOf(AlreadyExistsException.class)
                .hasMessageContaining("already exists");

        verify(customerRepository, never()).save(any());
    }

    @Test
    void shouldUpdateCustomerSuccessfullyWhenCustomerExists() {
        // given
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(sampleCustomer));

        // when
        customerManager.updateCustomer(sampleCustomer);

        // then
        verify(customerRepository, times(1)).save(sampleCustomer);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNullCustomer() {
        // when & then
        assertThatThrownBy(() -> customerManager.updateCustomer(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("is marked non-null but is null");

        verify(customerRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentCustomer() {
        // given
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> customerManager.updateCustomer(sampleCustomer))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("Customer not found");

        verify(customerRepository, never()).save(any());
    }

    @Test
    void shouldDeleteCustomerSuccessfullyWhenCustomerExists() {
        // given
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(sampleCustomer));

        // when
        customerManager.deleteCustomer(customerId);

        // then
        verify(customerRepository, times(1)).deleteById(customerId);
    }

    @Test
    void shouldThrowExceptionWhenDeletingWithNullId() {
        // when & then
        assertThatThrownBy(() -> customerManager.deleteCustomer(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("is marked non-null but is null");

        verify(customerRepository, never()).deleteById(anyString());
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentCustomer() {
        // given
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> customerManager.deleteCustomer(customerId))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("Customer not found");

        verify(customerRepository, never()).deleteById(anyString());
    }

    @Test
    void shouldReturnCustomerWhenItExistsInRepository() {
        // given
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(sampleCustomer));

        // when
        Optional<Customer> result = customerManager.findCustomer(customerId);

        // then
        assertThat(result).isPresent().contains(sampleCustomer);
        verify(customerRepository, times(1)).findById(customerId);
    }

    @Test
    void shouldReturnEmptyOptionalWhenCustomerDoesNotExist() {
        // given
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // when
        Optional<Customer> result = customerManager.findCustomer(customerId);

        // then
        assertThat(result).isEmpty();
        verify(customerRepository, times(1)).findById(customerId);
    }

    @Test
    void shouldReturnAllCustomersFromRepository() {
        // given
        List<Customer> mockList = List.of(
                sampleCustomer,
                new Customer("CU2", "Anna", "Nowak", "anna.nowak@example.com")
        );
        when(customerRepository.findAll()).thenReturn(mockList);

        // when
        List<Customer> result = customerManager.getAllCustomers();

        // then
        assertThat(result)
                .hasSize(2)
                .containsExactlyElementsOf(mockList);
        verify(customerRepository, times(1)).findAll();
    }
}