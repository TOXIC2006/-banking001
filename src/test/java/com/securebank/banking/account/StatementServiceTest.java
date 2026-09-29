package com.securebank.banking.account;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class StatementServiceTest {
    @Test
    void escapesCsvDescriptions() {
        StatementService service = new StatementService(mock(AccountService.class), mock(LedgerHistoryPort.class));
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        StatementService.Statement statement = new StatementService.Statement("100000000001", "Customer", "INR",
                now, now, List.of(new LedgerHistoryPort.StatementLine(now, "TXN-1", "DEBIT",
                "Bill, \"monthly\"", new BigDecimal("-10.00"), new BigDecimal("90.00"))));

        assertThat(service.asCsv(statement)).contains("\"Bill, \"\"monthly\"\"\"");
    }
}
