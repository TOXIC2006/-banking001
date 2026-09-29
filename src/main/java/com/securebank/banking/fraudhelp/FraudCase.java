package com.securebank.banking.fraudhelp;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "fraud_cases")
public class FraudCase {
    public enum Status {OPEN, REVIEWED, CONFIRMED, DISMISSED}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long customerId;
    @Column(length = 50)
    private String transactionReference;
    @Column(nullable = false, length = 255)
    private String reason;
    @Column(nullable = false)
    private int riskScore;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Status status;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    private Instant resolvedAt;

    protected FraudCase() {
    }

    public FraudCase(Long customerId, String transactionReference, String reason, int riskScore) {
        this.customerId = customerId;
        this.transactionReference = transactionReference;
        this.reason = reason;
        this.riskScore = riskScore;
        this.status = Status.OPEN;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getCustomerId() { return customerId; }
    public String getTransactionReference() { return transactionReference; }
    public String getReason() { return reason; }
    public int getRiskScore() { return riskScore; }
    public Status getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
