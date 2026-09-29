package com.securebank.banking.notification;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotificationRepository extends MongoRepository<NotificationMessage, String> {
    List<NotificationMessage> findTop100ByCustomerIdOrderByCreatedAtDesc(Long customerId);
}
