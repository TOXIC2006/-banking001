package com.securebank.banking.fraudhelp;

import com.securebank.banking.common.DomainEvents;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** Creates the ticket only after failed ledger work releases its account locks. */
@Component
public class FailedTransferTicketListener {
    private final HelpService help;

    public FailedTransferTicketListener(HelpService help) {
        this.help = help;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void onFailure(DomainEvents.TransferFailed event) {
        try {
            help.failedTransfer(event.customerId(), event.reference(), event.reason());
        } catch (RuntimeException ignored) {
            // Never replace the original transfer exception with a ticket-storage failure.
        }
    }
}
