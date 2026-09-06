package pl.adrian.electroshop.model.customer;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomerTest {

    @Test
    void shouldCreateCustomerWithValidData() {
        // when
        Customer customer = new Customer("1234", "Adrian", "Adrian", "Adrian@Adrian.com");

        // then
        assertThat(customer.getCustomerId()).isEqualTo("1234");
        assertThat(customer.getFirstName()).isEqualTo("Adrian");
    }

    @Test
    void shouldThrowExceptionWhenIdIsNull() {
        // when & then
        assertThatThrownBy(() -> new Customer(null, "Adrian", "Adrian", "Adrian@Adrian.com"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldAllowChangeIdButNoId() {
        // given
        Customer customer = new Customer("1234", "Adrian", "Adrian", "Adrian@Adrian.com");

        // when
        customer.setFirstName("Tomek");
        // customer.setCustomerId - blank

        // then
        assertThat(customer.getFirstName()).isEqualTo("Tomek");

    }
}