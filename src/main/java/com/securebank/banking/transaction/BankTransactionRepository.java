package com.securebank.banking.transaction;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BankTransactionRepository extends JpaRepository<BankTransaction, Long> {
    Optional<BankTransaction> findByIdempotencyKey(String idempotencyKey);
    Optional<BankTransaction> findByReference(String reference);
}
