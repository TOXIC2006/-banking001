package com.securebank.banking.account;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<CustomerAccount, Long> {
    Optional<CustomerAccount> findByEmailIgnoreCase(String email);
    Optional<CustomerAccount> findByAccountNumber(String accountNumber);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByAccountNumber(String accountNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from CustomerAccount a where a.id in :ids order by a.id")
    List<CustomerAccount> lockAllByIds(@Param("ids") List<Long> ids);
}
