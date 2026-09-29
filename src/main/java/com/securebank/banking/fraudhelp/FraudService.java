package com.securebank.banking.fraudhelp;

import com.securebank.banking.common.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class FraudService {
    private final FraudTransactionQuery transactions;
    private final FraudCaseRepository cases;
    private final BigDecimal singleLimit;
    private final BigDecimal dailyLimit;
    private final long velocityCount;

    public FraudService(FraudTransactionQuery transactions, FraudCaseRepository cases,
                        @Value("${banking.fraud.single-transaction-limit}") BigDecimal singleLimit,
                        @Value("${banking.fraud.daily-transaction-limit}") BigDecimal dailyLimit,
                        @Value("${banking.fraud.velocity-count}") long velocityCount) {
        this.transactions = transactions;
        this.cases = cases;
        this.singleLimit = singleLimit;
        this.dailyLimit = dailyLimit;
        this.velocityCount = velocityCount;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = BusinessException.class)
    public void assertAllowed(Long customerId, BigDecimal amount, String reference) {
        Instant oneDayAgo = Instant.now().minus(1, ChronoUnit.DAYS);
        String reason = null;
        int score = 0;
        if (amount.compareTo(singleLimit) > 0) {
            reason = "Single transaction limit exceeded";
            score = 95;
        } else if (transactions.debitTotalSince(customerId, oneDayAgo).add(amount).compareTo(dailyLimit) > 0) {
            reason = "Daily transaction amount anomaly";
            score = 90;
        } else if (transactions.debitCountSince(customerId, Instant.now().minus(10, ChronoUnit.MINUTES)) >= velocityCount) {
            reason = "Transaction velocity anomaly";
            score = 85;
        }
        if (reason != null) {
            cases.save(new FraudCase(customerId, reference, reason, score));
            throw new BusinessException(HttpStatus.FORBIDDEN, "FRAUD_BLOCKED",
                    "Transaction blocked for security review");
        }
    }
}
