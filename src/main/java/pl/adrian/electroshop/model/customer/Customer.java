package pl.adrian.electroshop.model.customer;

import lombok.Data;
import lombok.NonNull;

@Data
public class Customer {
    @NonNull private final String customerId;
    @NonNull private String firstName;
    @NonNull private String lastName;
    @NonNull private String email; // można pomyśleć nad jakąś walidacją formatu email
}
