package com.securebank.banking.transaction;

import com.securebank.banking.account.LedgerHistoryPort;
import com.securebank.banking.fraudhelp.FraudTransactionQuery;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Component
public class LedgerAdapters implements LedgerHistoryPort, FraudTransactionQuery {
    private final LedgerEntryRepository entries;

    public LedgerAdapters(LedgerEntryRepository entries) {
        this.entries = entries;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StatementLine> entries(Long accountId, Instant from, Instant to) {
        return entries.findByAccountIdAndCreatedAtBetweenOrderByCreatedAtAsc(accountId, from, to).stream()
                .map(entry -> new StatementLine(entry.getCreatedAt(), entry.getTransaction().getReference(),
                        entry.getEntryType().name(), entry.getDescription(),
                        entry.getEntryType() == LedgerEntry.EntryType.DEBIT
                                ? entry.getAmount().negate() : entry.getAmount(),
                        entry.getBalanceAfter()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long debitCountSince(Long customerId, Instant since) {
        return entries.countByAccountIdAndEntryTypeAndCreatedAtAfter(
                customerId, LedgerEntry.EntryType.DEBIT, since);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal debitTotalSince(Long customerId, Instant since) {
        BigDecimal total = entries.sumSince(customerId, LedgerEntry.EntryType.DEBIT, since);
        return total == null ? BigDecimal.ZERO : total;
    }
}
