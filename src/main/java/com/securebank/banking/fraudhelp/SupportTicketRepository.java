package com.securebank.banking.fraudhelp;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    List<SupportTicket> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
}
