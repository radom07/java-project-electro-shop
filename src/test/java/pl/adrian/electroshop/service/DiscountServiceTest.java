package pl.adrian.electroshop.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DiscountServiceTest {

    private DiscountService discountService;

    @BeforeEach
    void setUp() {
        discountService = new DiscountService();
    }

    @Test
    void shouldReturnDiscountPercentageWhenCodeIsValid() {
        // when
        Optional<BigDecimal> welcomeDiscount = discountService.getDiscountPercentage("WELCOME10");
        Optional<BigDecimal> adrianDiscount = discountService.getDiscountPercentage("ADRIAN20");

        // then
        assertThat(welcomeDiscount).isPresent().contains(new BigDecimal("0.10"));
        assertThat(adrianDiscount).isPresent().contains(new BigDecimal("0.20"));
    }

    @Test
    void shouldBeCaseInsensitiveWhenCheckingCode() {
        // when
        Optional<BigDecimal> discount = discountService.getDiscountPercentage("welcome10");

        // then
        assertThat(discount).isPresent().contains(new BigDecimal("0.10"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"INVALID", "FAKECODE"})
    void shouldReturnEmptyOptionalWhenCodeIsInvalid(String code) {
        // when
        Optional<BigDecimal> discount = discountService.getDiscountPercentage(code);

        // then
        assertThat(discount).isEmpty();
    }

    @Test
    void shouldReturnEmptyOptionalWhenCodeIsNull() {
        // when
        Optional<BigDecimal> discount = discountService.getDiscountPercentage(null);

        // then
        assertThat(discount).isEmpty();
    }

    @Test
    void shouldReturnEmptyOptionalWhenCodeIsBlank() {
        // when
        Optional<BigDecimal> discount = discountService.getDiscountPercentage("   ");

        // then
        assertThat(discount).isEmpty();
    }
}