package com.securebank.banking.transaction;

import com.securebank.banking.security.BankPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class TransactionController {
    private final TransactionService transactions;

    public TransactionController(TransactionService transactions) {
        this.transactions = transactions;
    }

    @PostMapping("/transactions/transfers")
    public ResponseEntity<TransactionDtos.Receipt> transfer(
            @AuthenticationPrincipal BankPrincipal principal,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody TransactionDtos.TransferRequest request) {
        return ResponseEntity.status(201)
                .body(transactions.transfer(principal.customerId(), request, idempotencyKey));
    }

    @PostMapping("/admin/accounts/{accountId}/fund")
    public ResponseEntity<TransactionDtos.Receipt> fund(
            @PathVariable Long accountId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody TransactionDtos.FundRequest request) {
        return ResponseEntity.status(201)
                .body(transactions.fundAccount(accountId, request, idempotencyKey));
    }
}
