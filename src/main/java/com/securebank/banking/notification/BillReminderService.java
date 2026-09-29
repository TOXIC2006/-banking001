package com.securebank.banking.notification;

import com.securebank.banking.account.AccountService;
import com.securebank.banking.common.BusinessException;
import com.securebank.banking.common.NotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class BillReminderService {
    private final BillReminderRepository reminders;
    private final AccountService accounts;
    private final ApplicationEventPublisher events;

    public BillReminderService(BillReminderRepository reminders, AccountService accounts,
                               ApplicationEventPublisher events) {
        this.reminders = reminders;
        this.accounts = accounts;
        this.events = events;
    }

    public BillReminder create(Long customerId, NotificationDtos.CreateReminder request) {
        if (!request.remindAt().isBefore(request.dueAt())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_REMINDER_TIME",
                    "Reminder time must be before the bill due time");
        }
        return reminders.save(new BillReminder(customerId, request.biller(), request.amount(),
                request.currency(), request.dueAt(), request.remindAt()));
    }

    public List<BillReminder> list(Long customerId) {
        return reminders.findByCustomerIdOrderByDueAtAsc(customerId);
    }

    public void cancel(Long customerId, String reminderId) {
        BillReminder reminder = reminders.findById(reminderId)
                .filter(value -> value.getCustomerId().equals(customerId))
                .orElseThrow(() -> new NotFoundException("Reminder not found"));
        reminders.delete(reminder);
    }

    @Scheduled(fixedDelayString = "${banking.notifications.reminder-delay-ms}")
    public void dispatchDue() {
        for (BillReminder reminder : reminders
                .findTop100ByEnabledTrueAndDispatchedFalseAndRemindAtLessThanEqualOrderByRemindAtAsc(Instant.now())) {
            try {
                String email = accounts.get(reminder.getCustomerId()).email();
                reminder.dispatched();
                reminders.save(reminder);
                events.publishEvent(new NotificationEvents.BillReminderDue(reminder, email));
            } catch (RuntimeException ignored) {
                // Leave the reminder undispatched so a later scheduler run can retry it.
            }
        }
    }
}
