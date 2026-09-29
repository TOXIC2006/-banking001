package com.securebank.banking.transaction;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;

public final class TransactionDtos {
    private TransactionDtos() {
    }

    public record TransferRequest(
            @NotBlank @Pattern(regexp = "^[0-9]{12}$") String destinationAccountNumber,
            @NotNull @DecimalMin(value = "0.01") @Digits(integer = 17, fraction = 2) BigDecimal amount,
            @Size(max = 255) String description) {
    }

    public record FundRequest(
            @NotNull @DecimalMin(value = "0.01") @Digits(integer = 17, fraction = 2) BigDecimal amount,
            @Size(max = 255) String description) {
    }

    public record Receipt(String reference, String type, String status, BigDecimal amount,
                          String currency, BigDecimal balanceAfter, Instant completedAt) {
    }
}
