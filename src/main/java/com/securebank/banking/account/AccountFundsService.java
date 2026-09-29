package com.securebank.banking.account;

import com.securebank.banking.common.BusinessException;
import com.securebank.banking.common.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Transaction-aware account balance API used directly by the Transaction module. */
@Service
public class AccountFundsService {
    private final AccountRepository accounts;

    public AccountFundsService(AccountRepository accounts) {
        this.accounts = accounts;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public FundsMovement transfer(Long ownerId, String destinationAccountNumber, BigDecimal amount) {
        CustomerAccount destination = accounts.findByAccountNumber(destinationAccountNumber)
                .orElseThrow(() -> new NotFoundException("Destination account not found"));
        if (ownerId.equals(destination.getId())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "SAME_ACCOUNT", "Source and destination must differ");
        }
        Map<Long, CustomerAccount> locked = lock(ownerId, destination.getId());
        CustomerAccount source = locked.get(ownerId);
        destination = locked.get(destination.getId());
        validateSource(source, amount);
        if (!source.getCurrency().equals(destination.getCurrency())) {
            throw new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, "CURRENCY_MISMATCH",
                    "Intra-bank accounts must use the same currency");
        }
        try {
            source.debit(amount);
        } catch (IllegalStateException ex) {
            throw new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, "INSUFFICIENT_FUNDS", ex.getMessage());
        }
        destination.credit(amount);
        return new FundsMovement(source.getId(), destination.getId(), source.getAccountNumber(),
                destination.getAccountNumber(), source.getBalance(), destination.getBalance(), source.getCurrency());
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public DebitMovement debitExternal(Long ownerId, BigDecimal amount) {
        CustomerAccount source = singleLock(ownerId);
        validateSource(source, amount);
        try {
            source.debit(amount);
        } catch (IllegalStateException ex) {
            throw new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, "INSUFFICIENT_FUNDS", ex.getMessage());
        }
        return new DebitMovement(source.getId(), source.getAccountNumber(), source.getBalance(), source.getCurrency());
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public DebitMovement credit(Long accountId, BigDecimal amount) {
        CustomerAccount account = singleLock(accountId);
        account.credit(amount);
        return new DebitMovement(account.getId(), account.getAccountNumber(), account.getBalance(), account.getCurrency());
    }

    private void validateSource(CustomerAccount account, BigDecimal amount) {
        if (!account.isEnabled()) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "ACCOUNT_DISABLED", "Account is disabled");
        }
        if (account.getKycStatus() != CustomerAccount.KycStatus.VERIFIED) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "KYC_REQUIRED", "Verified KYC is required");
        }
        if (amount.signum() <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_AMOUNT", "Amount must be positive");
        }
    }

    private CustomerAccount singleLock(Long id) {
        List<CustomerAccount> rows = accounts.lockAllByIds(List.of(id));
        if (rows.size() != 1) {
            throw new NotFoundException("Account not found");
        }
        return rows.getFirst();
    }

    private Map<Long, CustomerAccount> lock(Long first, Long second) {
        List<Long> ordered = first < second ? List.of(first, second) : List.of(second, first);
        List<CustomerAccount> rows = accounts.lockAllByIds(ordered);
        if (rows.size() != 2) {
            throw new NotFoundException("Account not found");
        }
        return rows.stream().collect(Collectors.toMap(CustomerAccount::getId, account -> account));
    }

    public record FundsMovement(Long sourceId, Long destinationId, String sourceNumber,
                                String destinationNumber, BigDecimal sourceBalance,
                                BigDecimal destinationBalance, String currency) {
    }

    public record DebitMovement(Long accountId, String accountNumber,
                                BigDecimal balanceAfter, String currency) {
    }
}
