package com.securebank.banking.account;

import com.securebank.banking.security.BankPrincipal;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/api")
public class AccountController {
    private final AccountService accounts;
    private final StatementService statements;

    public AccountController(AccountService accounts, StatementService statements) {
        this.accounts = accounts;
        this.statements = statements;
    }

    @PostMapping("/accounts/onboard")
    public ResponseEntity<AccountDtos.AccountView> onboard(@Valid @RequestBody AccountDtos.OnboardRequest request) {
        return ResponseEntity.status(201).body(accounts.onboard(request));
    }

    @GetMapping("/accounts/me")
    public AccountDtos.AccountView me(@AuthenticationPrincipal BankPrincipal principal) {
        return accounts.get(principal.customerId());
    }

    @PatchMapping("/accounts/me")
    public AccountDtos.AccountView update(@AuthenticationPrincipal BankPrincipal principal,
                                           @Valid @RequestBody AccountDtos.UpdateProfileRequest request) {
        return accounts.update(principal.customerId(), request);
    }

    @PostMapping("/accounts/me/kyc")
    public AccountDtos.AccountView submitKyc(@AuthenticationPrincipal BankPrincipal principal,
                                              @Valid @RequestBody AccountDtos.KycSubmission request) {
        return accounts.submitKyc(principal.customerId(), request);
    }

    @PutMapping("/admin/accounts/{customerId}/kyc")
    public AccountDtos.AccountView decideKyc(@PathVariable Long customerId,
                                              @Valid @RequestBody AccountDtos.KycDecision request) {
        return accounts.decideKyc(customerId, request.approved());
    }

    @GetMapping(value = "/accounts/me/statements", produces = "text/csv")
    public ResponseEntity<String> statement(
            @AuthenticationPrincipal BankPrincipal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        Instant end = to == null ? Instant.now() : to;
        Instant start = from == null ? end.minus(30, ChronoUnit.DAYS) : from;
        StatementService.Statement statement = statements.generate(principal.customerId(), start, end);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=statement-" + statement.accountNumber() + ".csv")
                .body(statements.asCsv(statement));
    }
}
