package com.securebank.banking.notification;

import org.springframework.stereotype.Component;

/** Local no-op sender; production deployments replace it with SMS/email provider adapters. */
@Component
public class SandboxMessageSender implements MessageSender {
    @Override
    public void send(NotificationMessage.Channel channel, String destination, String subject, String body) {
        // Deliberately does not log OTPs or personal data.
    }
}
