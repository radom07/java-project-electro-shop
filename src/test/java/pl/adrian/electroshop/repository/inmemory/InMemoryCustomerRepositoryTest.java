package pl.adrian.electroshop.repository.inmemory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.adrian.electroshop.model.customer.Customer;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemoryCustomerRepositoryTest {

    private InMemoryCustomerRepository repository;
    private Customer sampleCustomer;

    @BeforeEach
    void setUp() {
        repository = new InMemoryCustomerRepository();
        sampleCustomer = new Customer("CU1", "Jan", "Kowalski", "jan.kowalski@example.com");
    }

    @Test
    void shouldSaveAndFindCustomer() {
        // when
        repository.save(sampleCustomer);
        Optional<Customer> found = repository.findById("CU1");

        // then
        assertThat(found).isPresent().contains(sampleCustomer);
    }

    @Test
    void shouldOverwriteCustomerWhenSavingWithSameId() {
        // given
        repository.save(sampleCustomer);
        Customer updatedCustomer = new Customer("CU1", "Jan", "Nowak", "jan.nowak@example.com");

        // when
        repository.save(updatedCustomer);
        Optional<Customer> found = repository.findById("CU1");

        // then
        assertThat(found).isPresent().contains(updatedCustomer);
        assertThat(found.get().getLastName()).isEqualTo("Nowak");
    }

    @Test
    void shouldThrowExceptionWhenSavingNullCustomer() {
        // when & then
        assertThatThrownBy(() -> repository.save(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("is marked non-null but is null");
    }

    @Test
    void shouldDeleteCustomerById() {
        // given
        repository.save(sampleCustomer);

        // when
        repository.deleteById("CU1");
        Optional<Customer> found = repository.findById("CU1");

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void shouldNotThrowWhenDeletingNonExistentId() {
        // when & then
        assertThatCode(() -> repository.deleteById("CU1"))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldThrowExceptionWhenDeletingWithNullId() {
        // when & then
        assertThatThrownBy(() -> repository.deleteById(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("is marked non-null but is null");
    }

    @Test
    void shouldReturnEmptyOptionalWhenCustomerDoesNotExist() {
        // when
        Optional<Customer> found = repository.findById("NON_EXISTENT");

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void shouldThrowExceptionWhenFindingWithNullId() {
        // when & then
        assertThatThrownBy(() -> repository.findById(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("is marked non-null but is null");
    }

    @Test
    void shouldReturnAllSavedCustomers() {
        // given
        Customer secondCustomer = new Customer("CU2", "Anna", "Nowak", "anna.nowak@example.com");
        repository.save(sampleCustomer);
        repository.save(secondCustomer);

        // when
        List<Customer> all = repository.findAll();

        // then
        assertThat(all).hasSize(2).contains(sampleCustomer, secondCustomer);
    }

    @Test
    void shouldReturnEmptyListWhenNoCustomersSaved() {
        // when
        List<Customer> all = repository.findAll();

        // then
        assertThat(all).isEmpty();
    }

    @Test
    void shouldReturnUnmodifiableListFromFindAll() {
        // given
        repository.save(sampleCustomer);
        List<Customer> all = repository.findAll();

        // when & then
        Customer anotherCustomer = new Customer("CU2", "Anna", "Nowak", "anna.nowak@example.com");
        assertThatThrownBy(() -> all.add(anotherCustomer))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}