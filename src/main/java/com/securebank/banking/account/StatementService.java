package com.securebank.banking.account;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
public class StatementService {
    private final AccountService accountService;
    private final LedgerHistoryPort ledgerHistory;

    public StatementService(AccountService accountService, LedgerHistoryPort ledgerHistory) {
        this.accountService = accountService;
        this.ledgerHistory = ledgerHistory;
    }

    public Statement generate(Long customerId, Instant from, Instant to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("Statement start must be before end");
        }
        AccountDtos.AccountView account = accountService.get(customerId);
        return new Statement(account.accountNumber(), account.fullName(), account.currency(), from, to,
                ledgerHistory.entries(customerId, from, to));
    }

    public String asCsv(Statement statement) {
        StringBuilder csv = new StringBuilder("occurred_at,reference,type,description,amount,balance_after\n");
        for (LedgerHistoryPort.StatementLine line : statement.lines()) {
            csv.append(line.occurredAt()).append(',')
                    .append(escape(line.reference())).append(',')
                    .append(line.type()).append(',')
                    .append(escape(line.description())).append(',')
                    .append(line.amount()).append(',')
                    .append(line.balanceAfter()).append('\n');
        }
        return csv.toString();
    }

    private String escape(String value) {
        if (value == null) return "";
        return '"' + value.replace("\"", "\"\"") + '"';
    }

    public record Statement(String accountNumber, String customerName, String currency,
                            Instant from, Instant to, List<LedgerHistoryPort.StatementLine> lines) {
        public BigDecimal closingBalance() {
            return lines.isEmpty() ? BigDecimal.ZERO : lines.getLast().balanceAfter();
        }
    }
}
