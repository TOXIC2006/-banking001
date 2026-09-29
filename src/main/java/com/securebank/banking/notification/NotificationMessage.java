package com.securebank.banking.notification;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("notifications")
@CompoundIndex(name = "notification_customer_created", def = "{'customerId': 1, 'createdAt': -1}")
public class NotificationMessage {
    public enum Type {OTP, LOGIN_ALERT, TRANSACTION_RECEIPT, PAYMENT_RECEIPT, BILL_REMINDER}
    public enum Channel {EMAIL, SMS, PUSH}
    public enum Status {QUEUED, SENT, FAILED}

    @Id
    private String id;
    private Long customerId;
    private Type type;
    private Channel channel;
    private String maskedDestination;
    private String subject;
    private String content;
    private String relatedReference;
    private Status status;
    private String failureReason;
    private Instant createdAt;
    private Instant sentAt;

    public NotificationMessage(Long customerId, Type type, Channel channel, String maskedDestination,
                               String subject, String content, String relatedReference) {
        this.customerId = customerId;
        this.type = type;
        this.channel = channel;
        this.maskedDestination = maskedDestination;
        this.subject = subject;
        this.content = content;
        this.relatedReference = relatedReference;
        this.status = Status.QUEUED;
        this.createdAt = Instant.now();
    }

    public void sent() {
        this.status = Status.SENT;
        this.sentAt = Instant.now();
    }

    public void failed(String reason) {
        this.status = Status.FAILED;
        this.failureReason = reason;
    }

    public String getId() { return id; }
    public Long getCustomerId() { return customerId; }
    public Type getType() { return type; }
    public Channel getChannel() { return channel; }
    public String getMaskedDestination() { return maskedDestination; }
    public String getSubject() { return subject; }
    public String getContent() { return content; }
    public String getRelatedReference() { return relatedReference; }
    public Status getStatus() { return status; }
    public String getFailureReason() { return failureReason; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getSentAt() { return sentAt; }
}
