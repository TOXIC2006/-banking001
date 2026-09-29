package com.securebank.banking.notification;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;

public final class NotificationDtos {
    private NotificationDtos() {
    }

    public record OtpRequest(
            @NotNull NotificationMessage.Channel channel,
            @NotBlank @Size(max = 40) String purpose) {
    }

    public record OtpVerification(
            @NotBlank @Size(max = 40) String purpose,
            @NotBlank @Pattern(regexp = "^[0-9]{6}$") String code) {
    }

    public record CreateReminder(
            @NotBlank @Size(max = 160) String biller,
            @NotNull @DecimalMin("0.01") @Digits(integer = 17, fraction = 2) BigDecimal amount,
            @NotBlank @Pattern(regexp = "^[A-Z]{3}$") String currency,
            @NotNull @Future Instant dueAt,
            @NotNull @Future Instant remindAt) {
    }

    public record Accepted(String status) {
    }

    public record OtpResult(boolean valid) {
    }
}
