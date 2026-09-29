package com.securebank.banking.notification;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Document("bill_reminders")
@CompoundIndex(name = "reminder_due_idx", def = "{'enabled': 1, 'dispatched': 1, 'remindAt': 1}")
public class BillReminder {
    @Id
    private String id;
    private Long customerId;
    private String biller;
    private BigDecimal amount;
    private String currency;
    private Instant dueAt;
    private Instant remindAt;
    private boolean enabled;
    private boolean dispatched;
    private Instant createdAt;

    public BillReminder(Long customerId, String biller, BigDecimal amount, String currency,
                        Instant dueAt, Instant remindAt) {
        this.customerId = customerId;
        this.biller = biller;
        this.amount = amount;
        this.currency = currency;
        this.dueAt = dueAt;
        this.remindAt = remindAt;
        this.enabled = true;
        this.dispatched = false;
        this.createdAt = Instant.now();
    }

    public void dispatched() {
        this.dispatched = true;
    }

    public String getId() { return id; }
    public Long getCustomerId() { return customerId; }
    public String getBiller() { return biller; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public Instant getDueAt() { return dueAt; }
    public Instant getRemindAt() { return remindAt; }
    public boolean isEnabled() { return enabled; }
    public boolean isDispatched() { return dispatched; }
    public Instant getCreatedAt() { return createdAt; }
}
