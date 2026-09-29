package com.securebank.banking.account;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;

public final class AccountDtos {
    private AccountDtos() {
    }

    public record OnboardRequest(
            @NotBlank @Size(max = 160) String fullName,
            @NotBlank @Email @Size(max = 160) String email,
            @NotBlank @Pattern(regexp = "^[+]?[0-9]{8,15}$") String phone,
            @NotBlank @Size(min = 10, max = 72) String password,
            @NotBlank @Pattern(regexp = "^[A-Z]{3}$") String currency) {
    }

    public record UpdateProfileRequest(
            @NotBlank @Size(max = 160) String fullName,
            @NotBlank @Pattern(regexp = "^[+]?[0-9]{8,15}$") String phone,
            @Size(max = 500) String address) {
    }

    public record KycSubmission(
            @NotBlank @Pattern(regexp = "PASSPORT|NATIONAL_ID|DRIVING_LICENCE|PAN") String documentType,
            @NotBlank @Size(min = 4, max = 80) String documentNumber,
            @AssertTrue(message = "KYC consent is required") boolean consent) {
    }

    public record KycDecision(@NotNull Boolean approved) {
    }

    public record AccountView(Long id, String accountNumber, String email, String fullName,
                              String phone, String address, String role, String kycStatus,
                              BigDecimal balance, String currency, Instant createdAt) {
        static AccountView from(CustomerAccount account) {
            return new AccountView(account.getId(), account.getAccountNumber(), account.getEmail(),
                    account.getFullName(), account.getPhone(), account.getAddress(),
                    account.getRole().name(), account.getKycStatus().name(), account.getBalance(),
                    account.getCurrency(), account.getCreatedAt());
        }
    }
}
