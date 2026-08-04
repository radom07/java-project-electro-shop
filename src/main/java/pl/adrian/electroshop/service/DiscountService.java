package pl.adrian.electroshop.service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

public class DiscountService {

    private final Map<String, BigDecimal> discountCodes = Map.of(
            "WELCOME10", new BigDecimal("0.10"),
            "ADRIAN20", new BigDecimal("0.20")
    );

    public Optional<BigDecimal> getDiscountPercentage(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(discountCodes.get(code.toUpperCase()));
    }
}