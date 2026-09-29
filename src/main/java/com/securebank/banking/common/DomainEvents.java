package com.securebank.banking.common;

import java.math.BigDecimal;
import java.time.Instant;

public final class DomainEvents {
    private DomainEvents() {
    }

    public record LoginAlert(Long customerId, String email, String ipAddress, Instant occurredAt) {
    }

    public record TransferCompleted(Long customerId, String reference, BigDecimal amount,
                                    String currency, Instant occurredAt) {
    }

    public record TransferFailed(Long customerId, String reference, String reason, Instant occurredAt) {
    }

    public record PaymentUpdated(Long customerId, String reference, String status,
                                 BigDecimal amount, Instant occurredAt) {
    }
}
