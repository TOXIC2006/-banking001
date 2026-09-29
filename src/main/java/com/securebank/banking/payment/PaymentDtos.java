package com.securebank.banking.payment;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;

public final class PaymentDtos {
    private PaymentDtos() {
    }

    public record CreatePaymentRequest(
            @NotNull Payment.Rail rail,
            @NotBlank @Size(max = 160) String beneficiary,
            @Size(max = 40) String routingCode,
            @NotNull @DecimalMin(value = "0.01") @Digits(integer = 17, fraction = 2) BigDecimal amount) {
    }

    public record PaymentView(String reference, String rail, String beneficiary, String routingCode,
                              BigDecimal amount, String currency, String status, String providerReference,
                              String failureReason, Instant createdAt, Instant updatedAt) {
        static PaymentView from(Payment payment) {
            return new PaymentView(payment.getReference(), payment.getRail().name(), payment.getBeneficiary(),
                    payment.getRoutingCode(), payment.getAmount(), payment.getCurrency(), payment.getStatus().name(),
                    payment.getProviderReference(), payment.getFailureReason(), payment.getCreatedAt(),
                    payment.getUpdatedAt());
        }
    }
}
