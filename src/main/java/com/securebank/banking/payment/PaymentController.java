package com.securebank.banking.payment;

import com.securebank.banking.security.BankPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService payments;

    public PaymentController(PaymentService payments) {
        this.payments = payments;
    }

    @PostMapping
    public ResponseEntity<PaymentDtos.PaymentView> create(
            @AuthenticationPrincipal BankPrincipal principal,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody PaymentDtos.CreatePaymentRequest request) {
        return ResponseEntity.status(201).body(payments.create(principal.customerId(), request, idempotencyKey));
    }

    @GetMapping
    public List<PaymentDtos.PaymentView> list(@AuthenticationPrincipal BankPrincipal principal) {
        return payments.list(principal.customerId());
    }

    @GetMapping("/{reference}")
    public PaymentDtos.PaymentView get(@AuthenticationPrincipal BankPrincipal principal,
                                        @PathVariable String reference) {
        return payments.get(principal.customerId(), reference);
    }
}
