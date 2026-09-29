package com.securebank.banking.notification;

import com.securebank.banking.account.AccountDtos;
import com.securebank.banking.account.AccountService;
import com.securebank.banking.common.DomainEvents;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository notifications;
    private final MessageSender sender;
    private final AccountService accounts;

    public NotificationService(NotificationRepository notifications, MessageSender sender,
                               AccountService accounts) {
        this.notifications = notifications;
        this.sender = sender;
        this.accounts = accounts;
    }

    @Async
    @EventListener
    public void onLogin(DomainEvents.LoginAlert event) {
        dispatch(event.customerId(), NotificationMessage.Type.LOGIN_ALERT, NotificationMessage.Channel.EMAIL,
                event.email(), "New login", "A login to your account occurred from " + event.ipAddress(), null);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTransfer(DomainEvents.TransferCompleted event) {
        AccountDtos.AccountView account = accounts.get(event.customerId());
        dispatch(event.customerId(), NotificationMessage.Type.TRANSACTION_RECEIPT,
                NotificationMessage.Channel.EMAIL, account.email(), "Transfer receipt",
                "Transfer " + event.reference() + " completed for " + event.amount() + " " + event.currency(),
                event.reference());
    }

    @Async
    @EventListener
    public void onPayment(DomainEvents.PaymentUpdated event) {
        AccountDtos.AccountView account = accounts.get(event.customerId());
        dispatch(event.customerId(), NotificationMessage.Type.PAYMENT_RECEIPT,
                NotificationMessage.Channel.EMAIL, account.email(), "Payment update",
                "Payment " + event.reference() + " is " + event.status() + " for " + event.amount(),
                event.reference());
    }

    @Async
    @EventListener
    public void onOtp(NotificationEvents.OtpRequested event) {
        dispatch(event.customerId(), NotificationMessage.Type.OTP, event.channel(), event.destination(),
                "Your verification code", "Your " + event.purpose() + " code is " + event.code()
                        + ". It expires in 5 minutes.", null);
    }

    @Async
    @EventListener
    public void onReminder(NotificationEvents.BillReminderDue event) {
        BillReminder reminder = event.reminder();
        dispatch(reminder.getCustomerId(), NotificationMessage.Type.BILL_REMINDER,
                NotificationMessage.Channel.EMAIL, event.destination(), "Bill reminder",
                reminder.getBiller() + " payment of " + reminder.getAmount() + " "
                        + reminder.getCurrency() + " is due at " + reminder.getDueAt(), reminder.getId());
    }

    public List<NotificationMessage> list(Long customerId) {
        return notifications.findTop100ByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    private void dispatch(Long customerId, NotificationMessage.Type type, NotificationMessage.Channel channel,
                          String destination, String subject, String body, String reference) {
        NotificationMessage message = notifications.save(new NotificationMessage(customerId, type, channel,
                mask(destination), subject, redactStoredBody(type, body), reference));
        try {
            sender.send(channel, destination, subject, body);
            message.sent();
        } catch (RuntimeException failure) {
            String reason = failure.getMessage() == null ? "Provider unavailable" : failure.getMessage();
            message.failed(reason.substring(0, Math.min(reason.length(), 240)));
        }
        notifications.save(message);
    }

    private String redactStoredBody(NotificationMessage.Type type, String body) {
        return type == NotificationMessage.Type.OTP ? "One-time code dispatched; expires in 5 minutes." : body;
    }

    private String mask(String destination) {
        if (destination == null || destination.length() < 4) return "***";
        int at = destination.indexOf('@');
        if (at > 1) return destination.charAt(0) + "***" + destination.substring(at);
        return "***" + destination.substring(destination.length() - 4);
    }
}
