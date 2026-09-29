package com.securebank.banking.account;

import com.securebank.banking.common.BusinessException;
import com.securebank.banking.common.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

@Service
public class AccountService {
    private final AccountRepository accounts;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom random = new SecureRandom();

    public AccountService(AccountRepository accounts, PasswordEncoder passwordEncoder) {
        this.accounts = accounts;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AccountDtos.AccountView onboard(AccountDtos.OnboardRequest request) {
        if (accounts.existsByEmailIgnoreCase(request.email())) {
            throw new BusinessException(HttpStatus.CONFLICT, "EMAIL_EXISTS", "An account already uses this email");
        }
        CustomerAccount account = new CustomerAccount(nextAccountNumber(), request.email(),
                passwordEncoder.encode(request.password()), request.fullName(), request.phone(),
                request.currency(), CustomerAccount.Role.CUSTOMER);
        return AccountDtos.AccountView.from(accounts.save(account));
    }

    @Transactional(readOnly = true)
    public AccountDtos.AccountView get(Long customerId) {
        return AccountDtos.AccountView.from(require(customerId));
    }

    @Transactional
    public AccountDtos.AccountView update(Long customerId, AccountDtos.UpdateProfileRequest request) {
        CustomerAccount account = require(customerId);
        account.updateProfile(request.fullName(), request.phone(), request.address());
        return AccountDtos.AccountView.from(account);
    }

    @Transactional
    public AccountDtos.AccountView submitKyc(Long customerId, AccountDtos.KycSubmission request) {
        CustomerAccount account = require(customerId);
        account.submitKyc(request.documentType(), hashDocument(request.documentNumber()));
        return AccountDtos.AccountView.from(account);
    }

    @Transactional
    public AccountDtos.AccountView decideKyc(Long customerId, boolean approved) {
        CustomerAccount account = require(customerId);
        if (account.getKycStatus() != CustomerAccount.KycStatus.UNDER_REVIEW) {
            throw new BusinessException(HttpStatus.CONFLICT, "KYC_NOT_PENDING", "KYC is not awaiting review");
        }
        account.decideKyc(approved);
        return AccountDtos.AccountView.from(account);
    }

    @Transactional(readOnly = true)
    public CustomerAccount require(Long id) {
        return accounts.findById(id).orElseThrow(() -> new NotFoundException("Account not found"));
    }

    @Transactional
    public void ensureAdmin(String email, String rawPassword) {
        if (email == null || email.isBlank() || rawPassword == null || rawPassword.isBlank()
                || accounts.existsByEmailIgnoreCase(email)) {
            return;
        }
        accounts.save(new CustomerAccount(nextAccountNumber(), email, passwordEncoder.encode(rawPassword),
                "Bank Administrator", "+910000000000", "INR", CustomerAccount.Role.ADMIN));
    }

    private String nextAccountNumber() {
        for (int attempt = 0; attempt < 20; attempt++) {
            String value = "10" + String.format("%010d", random.nextLong(10_000_000_000L));
            if (!accounts.existsByAccountNumber(value)) {
                return value;
            }
        }
        throw new IllegalStateException("Unable to allocate account number");
    }

    private String hashDocument(String number) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(number.trim().toUpperCase().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }
}
