package com.securebank.banking.payment;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Document("payments")
@CompoundIndex(name = "customer_created_idx", def = "{'customerId': 1, 'createdAt': -1}")
public class Payment {
    public enum Rail {IMPS, NEFT, PAYMENT_GATEWAY, UTILITY}
    public enum Status {INITIATED, DEBITED, COMPLETED, FAILED, REFUNDED, RECONCILIATION_REQUIRED}

    @Id
    private String id;
    @Indexed(unique = true)
    private String reference;
    @Indexed(unique = true)
    private String idempotencyKey;
    private Long customerId;
    private Rail rail;
    private String beneficiary;
    private String routingCode;
    private BigDecimal amount;
    private String currency;
    private Status status;
    private String providerReference;
    private String failureReason;
    private Instant createdAt;
    private Instant updatedAt;

    public Payment(String reference, String idempotencyKey, Long customerId, Rail rail,
                   String beneficiary, String routingCode, BigDecimal amount) {
        this.reference = reference;
        this.idempotencyKey = idempotencyKey;
        this.customerId = customerId;
        this.rail = rail;
        this.beneficiary = beneficiary;
        this.routingCode = routingCode;
        this.amount = amount;
        this.status = Status.INITIATED;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public void debited(String currency) {
        this.currency = currency;
        this.status = Status.DEBITED;
        this.updatedAt = Instant.now();
    }

    public void complete(String providerReference) {
        this.providerReference = providerReference;
        this.status = Status.COMPLETED;
        this.updatedAt = Instant.now();
    }

    public void fail(String safeReason) {
        this.failureReason = safeReason;
        this.status = Status.FAILED;
        this.updatedAt = Instant.now();
    }

    public void refunded(String safeReason) {
        this.failureReason = safeReason;
        this.status = Status.REFUNDED;
        this.updatedAt = Instant.now();
    }

    public void reconciliationRequired(String safeReason) {
        this.failureReason = safeReason;
        this.status = Status.RECONCILIATION_REQUIRED;
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public String getReference() { return reference; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public Long getCustomerId() { return customerId; }
    public Rail getRail() { return rail; }
    public String getBeneficiary() { return beneficiary; }
    public String getRoutingCode() { return routingCode; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public Status getStatus() { return status; }
    public String getProviderReference() { return providerReference; }
    public String getFailureReason() { return failureReason; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
