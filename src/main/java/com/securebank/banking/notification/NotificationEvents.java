package com.securebank.banking.notification;

public final class NotificationEvents {
    private NotificationEvents() {
    }

    record OtpRequested(Long customerId, NotificationMessage.Channel channel,
                        String destination, String purpose, String code) {
    }

    record BillReminderDue(BillReminder reminder, String destination) {
    }
}
