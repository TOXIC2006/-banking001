package com.securebank.banking.transaction;

import com.securebank.banking.account.AccountFundsService;
import com.securebank.banking.common.BusinessException;
import com.securebank.banking.common.DomainEvents;
import com.securebank.banking.fraudhelp.FraudService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionService {
    private final AccountFundsService funds;
    private final BankTransactionRepository transactions;
    private final LedgerEntryRepository ledger;
    private final FraudService fraud;
    private final ApplicationEventPublisher events;

    public TransactionService(AccountFundsService funds, BankTransactionRepository transactions,
                              LedgerEntryRepository ledger, FraudService fraud,
                              ApplicationEventPublisher events) {
        this.funds = funds;
        this.transactions = transactions;
        this.ledger = ledger;
        this.fraud = fraud;
        this.events = events;
    }

    @Transactional
    public TransactionDtos.Receipt transfer(Long customerId, TransactionDtos.TransferRequest request,
                                            String idempotencyKey) {
        String key = requireIdempotencyKey(idempotencyKey);
        BankTransaction existing = transactions.findByIdempotencyKey(key).orElse(null);
        if (existing != null) {
            assertOwnedAndEquivalent(existing, customerId, request.amount());
            return receipt(existing, customerId);
        }

        String reference = nextReference("TXN");
        try {
            fraud.assertAllowed(customerId, request.amount(), reference);
            AccountFundsService.FundsMovement movement =
                    funds.transfer(customerId, request.destinationAccountNumber(), request.amount());
            BankTransaction transaction = transactions.save(new BankTransaction(reference, key,
                    movement.sourceId(), movement.destinationId(), BankTransaction.Type.INTRA_BANK,
                    request.amount(), movement.currency(), safeDescription(request.description(), "Intra-bank transfer")));
            ledger.saveAll(List.of(
                    new LedgerEntry(movement.sourceId(), transaction, LedgerEntry.EntryType.DEBIT,
                            request.amount(), movement.sourceBalance(), transaction.getDescription()),
                    new LedgerEntry(movement.destinationId(), transaction, LedgerEntry.EntryType.CREDIT,
                            request.amount(), movement.destinationBalance(), transaction.getDescription())));
            events.publishEvent(new DomainEvents.TransferCompleted(customerId, reference, request.amount(),
                    movement.currency(), Instant.now()));
            return toReceipt(transaction, movement.sourceBalance());
        } catch (RuntimeException ex) {
            recordFailure(customerId, reference, ex);
            throw ex;
        }
    }

    /** Direct internal API used by Payment. Debit and ledger entry commit atomically in MySQL. */
    @Transactional
    public TransactionDtos.Receipt debitForExternalPayment(Long customerId, BigDecimal amount,
                                                            String idempotencyKey, String reference,
                                                            String description) {
        String key = requireIdempotencyKey(idempotencyKey);
        BankTransaction existing = transactions.findByIdempotencyKey(key).orElse(null);
        if (existing != null) {
            assertOwnedAndEquivalent(existing, customerId, amount);
            return receipt(existing, customerId);
        }
        try {
            fraud.assertAllowed(customerId, amount, reference);
            AccountFundsService.DebitMovement movement = funds.debitExternal(customerId, amount);
            BankTransaction transaction = transactions.save(new BankTransaction(reference, key, customerId,
                    null, BankTransaction.Type.EXTERNAL_DEBIT, amount, movement.currency(),
                    safeDescription(description, "External payment")));
            ledger.save(new LedgerEntry(customerId, transaction, LedgerEntry.EntryType.DEBIT,
                    amount, movement.balanceAfter(), transaction.getDescription()));
            return toReceipt(transaction, movement.balanceAfter());
        } catch (RuntimeException ex) {
            recordFailure(customerId, reference, ex);
            throw ex;
        }
    }

    /** Compensating transaction for an external provider failure; never rewrites ledger history. */
    @Transactional
    public TransactionDtos.Receipt reverseExternalPayment(Long customerId, BigDecimal amount,
                                                            String originalReference) {
        String key = "reversal:" + originalReference;
        BankTransaction existing = transactions.findByIdempotencyKey(key).orElse(null);
        if (existing != null) {
            return receipt(existing, customerId);
        }
        AccountFundsService.DebitMovement movement = funds.credit(customerId, amount);
        BankTransaction transaction = transactions.save(new BankTransaction(nextReference("REV"), key,
                null, customerId, BankTransaction.Type.REVERSAL, amount, movement.currency(),
                "Reversal for " + originalReference));
        ledger.save(new LedgerEntry(customerId, transaction, LedgerEntry.EntryType.CREDIT,
                amount, movement.balanceAfter(), transaction.getDescription()));
        return toReceipt(transaction, movement.balanceAfter());
    }

    @Transactional
    public TransactionDtos.Receipt fundAccount(Long accountId, TransactionDtos.FundRequest request,
                                               String idempotencyKey) {
        String key = requireIdempotencyKey(idempotencyKey);
        BankTransaction existing = transactions.findByIdempotencyKey(key).orElse(null);
        if (existing != null) {
            if (!accountId.equals(existing.getDestinationAccountId())
                    || request.amount().compareTo(existing.getAmount()) != 0) {
                throw idempotencyConflict();
            }
            return receipt(existing, accountId);
        }
        AccountFundsService.DebitMovement movement = funds.credit(accountId, request.amount());
        BankTransaction transaction = transactions.save(new BankTransaction(nextReference("DEP"), key,
                null, accountId, BankTransaction.Type.DEPOSIT, request.amount(), movement.currency(),
                safeDescription(request.description(), "Administrative deposit")));
        ledger.save(new LedgerEntry(accountId, transaction, LedgerEntry.EntryType.CREDIT,
                request.amount(), movement.balanceAfter(), transaction.getDescription()));
        return toReceipt(transaction, movement.balanceAfter());
    }

    private TransactionDtos.Receipt receipt(BankTransaction transaction, Long accountId) {
        BigDecimal balanceAfter = ledger.findByTransactionIdAndAccountId(transaction.getId(), accountId)
                .map(LedgerEntry::getBalanceAfter).orElse(BigDecimal.ZERO);
        return toReceipt(transaction, balanceAfter);
    }

    private TransactionDtos.Receipt toReceipt(BankTransaction transaction, BigDecimal balanceAfter) {
        return new TransactionDtos.Receipt(transaction.getReference(), transaction.getTransactionType().name(),
                transaction.getStatus().name(), transaction.getAmount(), transaction.getCurrency(), balanceAfter,
                transaction.getCompletedAt());
    }

    private void assertOwnedAndEquivalent(BankTransaction transaction, Long customerId, BigDecimal amount) {
        if (!customerId.equals(transaction.getSourceAccountId())
                || transaction.getAmount().compareTo(amount) != 0) {
            throw idempotencyConflict();
        }
    }

    private BusinessException idempotencyConflict() {
        return new BusinessException(HttpStatus.CONFLICT, "IDEMPOTENCY_CONFLICT",
                "Idempotency-Key was already used for a different operation");
    }

    private String requireIdempotencyKey(String value) {
        if (value == null || value.isBlank() || value.length() > 100) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_IDEMPOTENCY_KEY",
                    "Idempotency-Key must contain 1 to 100 characters");
        }
        return value.trim();
    }

    private String nextReference(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase();
    }

    private String safeDescription(String value, String fallback) {
        if (value == null || value.isBlank()) return fallback;
        return value.substring(0, Math.min(value.length(), 255));
    }

    private void recordFailure(Long customerId, String reference, RuntimeException ex) {
        String reason = ex instanceof BusinessException && ex.getMessage() != null
                ? ex.getMessage() : "Transfer processing failed";
        events.publishEvent(new DomainEvents.TransferFailed(customerId, reference,
                reason.substring(0, Math.min(reason.length(), 900)), Instant.now()));
    }
}
