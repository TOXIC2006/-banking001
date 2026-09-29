package com.securebank.banking.account;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** Implemented by the transaction module to keep statement generation owned by Account. */
public interface LedgerHistoryPort {
    List<StatementLine> entries(Long accountId, Instant from, Instant to);

    record StatementLine(Instant occurredAt, String reference, String type,
                         String description, BigDecimal amount, BigDecimal balanceAfter) {
    }
}
