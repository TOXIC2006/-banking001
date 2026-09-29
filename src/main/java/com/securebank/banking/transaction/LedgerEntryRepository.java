package com.securebank.banking.transaction;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {
    @EntityGraph(attributePaths = "transaction")
    List<LedgerEntry> findByAccountIdAndCreatedAtBetweenOrderByCreatedAtAsc(
            Long accountId, Instant from, Instant to);

    long countByAccountIdAndEntryTypeAndCreatedAtAfter(
            Long accountId, LedgerEntry.EntryType entryType, Instant since);

    java.util.Optional<LedgerEntry> findByTransactionIdAndAccountId(Long transactionId, Long accountId);

    @Query("select coalesce(sum(e.amount), 0) from LedgerEntry e " +
            "where e.accountId = :accountId and e.entryType = :type and e.createdAt >= :since")
    BigDecimal sumSince(@Param("accountId") Long accountId,
                        @Param("type") LedgerEntry.EntryType type,
                        @Param("since") Instant since);
}
