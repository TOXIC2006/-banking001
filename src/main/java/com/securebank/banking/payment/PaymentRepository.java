package com.securebank.banking.payment;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends MongoRepository<Payment, String> {
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);
    Optional<Payment> findByReferenceAndCustomerId(String reference, Long customerId);
    List<Payment> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
}
