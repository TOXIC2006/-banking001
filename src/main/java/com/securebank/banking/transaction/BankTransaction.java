package com.securebank.banking.transaction;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "bank_transactions")
public class BankTransaction {
    public enum Type {INTRA_BANK, EXTERNAL_DEBIT, REVERSAL, DEPOSIT}
    public enum Status {PENDING, COMPLETED, FAILED, REVERSED}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 50)
    private String reference;
    @Column(nullable = false, unique = true, length = 100)
    private String idempotencyKey;
    private Long sourceAccountId;
    private Long destinationAccountId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Type transactionType;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    @Column(nullable = false, length = 3)
    private String currency;
    @Column(length = 255)
    private String description;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Status status;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    private Instant completedAt;

    protected BankTransaction() {
    }

    public BankTransaction(String reference, String idempotencyKey, Long sourceAccountId,
                           Long destinationAccountId, Type type, BigDecimal amount,
                           String currency, String description) {
        this.reference = reference;
        this.idempotencyKey = idempotencyKey;
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
        this.transactionType = type;
        this.amount = amount;
        this.currency = currency;
        this.description = description;
        this.status = Status.COMPLETED;
        this.createdAt = Instant.now();
        this.completedAt = this.createdAt;
    }

    public Long getId() { return id; }
    public String getReference() { return reference; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public Long getSourceAccountId() { return sourceAccountId; }
    public Long getDestinationAccountId() { return destinationAccountId; }
    public Type getTransactionType() { return transactionType; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getDescription() { return description; }
    public Status getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getCompletedAt() { return completedAt; }
}
