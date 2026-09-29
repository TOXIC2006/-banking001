package com.securebank.banking.fraudhelp;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FraudCaseRepository extends JpaRepository<FraudCase, Long> {
    List<FraudCase> findByStatusOrderByCreatedAtDesc(FraudCase.Status status);
}
