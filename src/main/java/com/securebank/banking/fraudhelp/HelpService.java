package com.securebank.banking.fraudhelp;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class HelpService {
    private final SupportTicketRepository tickets;
    private final FraudCaseRepository fraudCases;

    public HelpService(SupportTicketRepository tickets, FraudCaseRepository fraudCases) {
        this.tickets = tickets;
        this.fraudCases = fraudCases;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failedTransfer(Long customerId, String reference, String safeReason) {
        tickets.save(new SupportTicket(nextTicketNumber(), customerId, reference, "FAILED_TRANSFER",
                "Transfer requires assistance", safeDetails(safeReason)));
    }

    @Transactional(readOnly = true)
    public List<TicketView> customerTickets(Long customerId) {
        return tickets.findByCustomerIdOrderByCreatedAtDesc(customerId).stream().map(TicketView::from).toList();
    }

    @Transactional(readOnly = true)
    public List<FraudCase> openFraudCases() {
        return fraudCases.findByStatusOrderByCreatedAtDesc(FraudCase.Status.OPEN);
    }

    private String nextTicketNumber() {
        return "HELP-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }

    private String safeDetails(String reason) {
        if (reason == null || reason.isBlank()) return "The transfer did not complete.";
        return reason.substring(0, Math.min(reason.length(), 900));
    }

    public record TicketView(String ticketNumber, String transactionReference, String category,
                             String subject, String details, String status, java.time.Instant createdAt) {
        static TicketView from(SupportTicket ticket) {
            return new TicketView(ticket.getTicketNumber(), ticket.getTransactionReference(), ticket.getCategory(),
                    ticket.getSubject(), ticket.getDetails(), ticket.getStatus().name(), ticket.getCreatedAt());
        }
    }
}
