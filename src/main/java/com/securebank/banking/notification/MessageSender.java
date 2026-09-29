package com.securebank.banking.notification;

/** Adapter seam for SMS, email, and push vendors. */
public interface MessageSender {
    void send(NotificationMessage.Channel channel, String destination, String subject, String body);
}
