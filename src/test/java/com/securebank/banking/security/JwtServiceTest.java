package com.securebank.banking.security;

import com.securebank.banking.account.CustomerAccount;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {
    @Test
    void issuesAndValidatesToken() {
        JwtService service = new JwtService("a-secure-test-secret-that-is-longer-than-32-bytes", Duration.ofMinutes(5));
        BankPrincipal principal = new BankPrincipal(42L, "user@example.com", "hash", true,
                CustomerAccount.Role.CUSTOMER);

        String token = service.issue(principal);

        assertThat(service.username(token)).isEqualTo("user@example.com");
        assertThat(service.isValid(token, principal)).isTrue();
    }

    @Test
    void rejectsShortSecret() {
        assertThatThrownBy(() -> new JwtService("short", Duration.ofMinutes(5)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
