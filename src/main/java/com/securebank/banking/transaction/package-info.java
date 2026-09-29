/**
 * ACID ledger and fund movement module. All balance changes and matching ledger entries
 * commit in one MySQL transaction using deterministic pessimistic account locking.
 */
package com.securebank.banking.transaction;
