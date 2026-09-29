package com.securebank.banking.account;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "customer_accounts")
public class CustomerAccount {
    public enum Role {CUSTOMER, ADMIN}
    public enum KycStatus {NOT_SUBMITTED, UNDER_REVIEW, VERIFIED, REJECTED}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String accountNumber;

    @Column(nullable = false, unique = true, length = 160)
    private String email;

    @Column(nullable = false, length = 100)
    private String passwordHash;

    @Column(nullable = false, length = 160)
    private String fullName;

    @Column(nullable = false, length = 24)
    private String phone;

    @Column(length = 500)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private KycStatus kycStatus;

    @Column(length = 40)
    private String kycDocumentType;

    @Column(length = 100)
    private String kycDocumentNumber;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private long version;

    protected CustomerAccount() {
    }

    public CustomerAccount(String accountNumber, String email, String passwordHash, String fullName,
                           String phone, String currency, Role role) {
        this.accountNumber = accountNumber;
        this.email = email.toLowerCase();
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.phone = phone;
        this.currency = currency.toUpperCase();
        this.role = role;
        this.kycStatus = KycStatus.NOT_SUBMITTED;
        this.balance = BigDecimal.ZERO.setScale(2);
        this.enabled = true;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public void updateProfile(String fullName, String phone, String address) {
        this.fullName = fullName;
        this.phone = phone;
        this.address = address;
        this.updatedAt = Instant.now();
    }

    public void submitKyc(String documentType, String documentHash) {
        this.kycDocumentType = documentType;
        this.kycDocumentNumber = documentHash;
        this.kycStatus = KycStatus.UNDER_REVIEW;
        this.updatedAt = Instant.now();
    }

    public void decideKyc(boolean approved) {
        this.kycStatus = approved ? KycStatus.VERIFIED : KycStatus.REJECTED;
        this.updatedAt = Instant.now();
    }

    void debit(BigDecimal amount) {
        if (balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds");
        }
        balance = balance.subtract(amount);
        updatedAt = Instant.now();
    }

    void credit(BigDecimal amount) {
        balance = balance.add(amount);
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getAccountNumber() { return accountNumber; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public Role getRole() { return role; }
    public KycStatus getKycStatus() { return kycStatus; }
    public BigDecimal getBalance() { return balance; }
    public String getCurrency() { return currency; }
    public boolean isEnabled() { return enabled; }
    public Instant getCreatedAt() { return createdAt; }
}
