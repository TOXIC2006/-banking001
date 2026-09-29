package com.securebank.banking.fraudhelp;

import java.math.BigDecimal;
import java.time.Instant;

/** Implemented inside Transaction so Fraud does not reach into ledger repositories. */
public interface FraudTransactionQuery {
    long debitCountSince(Long customerId, Instant since);
    BigDecimal debitTotalSince(Long customerId, Instant since);
}
