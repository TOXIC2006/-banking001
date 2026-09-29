package com.securebank.banking.payment;

import com.securebank.banking.common.BusinessException;
import com.securebank.banking.common.DomainEvents;
import com.securebank.banking.common.NotFoundException;
import com.securebank.banking.fraudhelp.HelpService;
import com.securebank.banking.transaction.TransactionDtos;
import com.securebank.banking.transaction.TransactionService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {
    private final PaymentRepository payments;
    private final TransactionService transactions;
    private final ExternalPaymentGateway gateway;
    private final HelpService help;
    private final ApplicationEventPublisher events;

    public PaymentService(PaymentRepository payments, TransactionService transactions,
                          ExternalPaymentGateway gateway, HelpService help,
                          ApplicationEventPublisher events) {
        this.payments = payments;
        this.transactions = transactions;
        this.gateway = gateway;
        this.help = help;
        this.events = events;
    }

    public PaymentDtos.PaymentView create(Long customerId, PaymentDtos.CreatePaymentRequest request,
                                           String idempotencyKey) {
        validateRail(request);
        String key = validateIdempotencyKey(idempotencyKey);
        Payment existing = payments.findByIdempotencyKey(key).orElse(null);
        if (existing != null) {
            assertEquivalent(existing, customerId, request);
            return PaymentDtos.PaymentView.from(existing);
        }

        Payment payment = new Payment(nextReference(), key, customerId, request.rail(),
                request.beneficiary(), request.routingCode(), request.amount());
        try {
            payment = payments.save(payment);
        } catch (DuplicateKeyException race) {
            Payment concurrent = payments.findByIdempotencyKey(key).orElseThrow(() -> race);
            assertEquivalent(concurrent, customerId, request);
            return PaymentDtos.PaymentView.from(concurrent);
        }

        TransactionDtos.Receipt debit;
        try {
            debit = transactions.debitForExternalPayment(customerId, request.amount(),
                    "payment-debit:" + digest(key), payment.getReference(), request.rail() + " payment");
        } catch (RuntimeException debitFailure) {
            payment.fail(safeReason(debitFailure));
            saveBestEffort(payment);
            publish(payment);
            throw debitFailure;
        }

        payment.debited(debit.currency());
        try {
            payments.save(payment);
        } catch (RuntimeException stateFailure) {
            compensate(payment, stateFailure);
            throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE, "PAYMENT_STATE_UNAVAILABLE",
                    "Payment state could not be recorded; debited funds were returned");
        }

        ExternalPaymentGateway.SettlementResult result;
        try {
            result = gateway.settle(payment.getReference(), payment.getRail(), payment.getBeneficiary(),
                    payment.getRoutingCode(), payment.getAmount(), payment.getCurrency());
        } catch (RuntimeException providerFailure) {
            compensate(payment, providerFailure);
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "PAYMENT_PROVIDER_FAILED",
                    "External settlement failed; consult the payment status for the reversal outcome");
        }

        payment.complete(result.providerReference());
        try {
            payments.save(payment);
        } catch (RuntimeException stateFailure) {
            recordHelp(payment, "Provider settled the payment but final status persistence failed");
            throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE, "PAYMENT_RECONCILIATION_REQUIRED",
                    "Provider settlement succeeded but status reconciliation is required");
        }
        publish(payment);
        return PaymentDtos.PaymentView.from(payment);
    }

    public List<PaymentDtos.PaymentView> list(Long customerId) {
        return payments.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(PaymentDtos.PaymentView::from).toList();
    }

    public PaymentDtos.PaymentView get(Long customerId, String reference) {
        return payments.findByReferenceAndCustomerId(reference, customerId)
                .map(PaymentDtos.PaymentView::from)
                .orElseThrow(() -> new NotFoundException("Payment not found"));
    }

    private void compensate(Payment payment, RuntimeException failure) {
        String reason = safeReason(failure);
        try {
            transactions.reverseExternalPayment(payment.getCustomerId(), payment.getAmount(), payment.getReference());
            payment.refunded(reason);
        } catch (RuntimeException reversalFailure) {
            payment.reconciliationRequired("Automatic reversal requires manual reconciliation");
        }
        saveBestEffort(payment);
        recordHelp(payment, reason);
        publish(payment);
    }

    private void saveBestEffort(Payment payment) {
        try {
            payments.save(payment);
        } catch (RuntimeException ignored) {
            // A MySQL support ticket remains as a reconciliation signal when MongoDB is unavailable.
        }
    }

    private void recordHelp(Payment payment, String reason) {
        try {
            help.failedTransfer(payment.getCustomerId(), payment.getReference(), reason);
        } catch (RuntimeException ignored) {
            // Keep the original payment response if support persistence is temporarily unavailable.
        }
    }

    private void publish(Payment payment) {
        events.publishEvent(new DomainEvents.PaymentUpdated(payment.getCustomerId(), payment.getReference(),
                payment.getStatus().name(), payment.getAmount(), Instant.now()));
    }

    private void assertEquivalent(Payment payment, Long customerId, PaymentDtos.CreatePaymentRequest request) {
        if (!payment.getCustomerId().equals(customerId) || payment.getRail() != request.rail()
                || payment.getAmount().compareTo(request.amount()) != 0
                || !payment.getBeneficiary().equals(request.beneficiary())) {
            throw new BusinessException(HttpStatus.CONFLICT, "IDEMPOTENCY_CONFLICT",
                    "Idempotency-Key was already used for a different payment");
        }
    }

    private void validateRail(PaymentDtos.CreatePaymentRequest request) {
        if ((request.rail() == Payment.Rail.IMPS || request.rail() == Payment.Rail.NEFT)
                && (request.routingCode() == null || request.routingCode().isBlank())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "ROUTING_CODE_REQUIRED",
                    "IMPS and NEFT payments require a routing code");
        }
    }

    private String validateIdempotencyKey(String value) {
        if (value == null || value.isBlank() || value.length() > 100) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_IDEMPOTENCY_KEY",
                    "Idempotency-Key must contain 1 to 100 characters");
        }
        return value.trim();
    }

    private String digest(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }

    private String nextReference() {
        return "PAY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase();
    }

    private String safeReason(RuntimeException failure) {
        boolean safeType = failure instanceof BusinessException
                || failure instanceof ExternalPaymentGateway.ProviderException;
        String value = safeType ? failure.getMessage() : null;
        if (value == null || value.isBlank()) return "Payment processing failed";
        return value.substring(0, Math.min(value.length(), 240));
    }
}
