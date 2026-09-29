package com.securebank.banking.notification;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface BillReminderRepository extends MongoRepository<BillReminder, String> {
    List<BillReminder> findTop100ByEnabledTrueAndDispatchedFalseAndRemindAtLessThanEqualOrderByRemindAtAsc(Instant now);
    List<BillReminder> findByCustomerIdOrderByDueAtAsc(Long customerId);
}
