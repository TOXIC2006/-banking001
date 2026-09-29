package com.securebank.banking.fraudhelp;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "support_tickets")
public class SupportTicket {
    public enum Status {OPEN, IN_PROGRESS, RESOLVED, CLOSED}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 40)
    private String ticketNumber;
    @Column(nullable = false)
    private Long customerId;
    @Column(length = 50)
    private String transactionReference;
    @Column(nullable = false, length = 40)
    private String category;
    @Column(nullable = false, length = 160)
    private String subject;
    @Column(nullable = false, length = 1000)
    private String details;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Status status;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private Instant updatedAt;

    protected SupportTicket() {
    }

    public SupportTicket(String ticketNumber, Long customerId, String transactionReference,
                         String category, String subject, String details) {
        this.ticketNumber = ticketNumber;
        this.customerId = customerId;
        this.transactionReference = transactionReference;
        this.category = category;
        this.subject = subject;
        this.details = details;
        this.status = Status.OPEN;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public Long getId() { return id; }
    public String getTicketNumber() { return ticketNumber; }
    public Long getCustomerId() { return customerId; }
    public String getTransactionReference() { return transactionReference; }
    public String getCategory() { return category; }
    public String getSubject() { return subject; }
    public String getDetails() { return details; }
    public Status getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
