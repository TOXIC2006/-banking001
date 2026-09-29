package com.securebank.banking.config;

import com.securebank.banking.account.AccountService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap implements ApplicationRunner {
    private final AccountService accounts;
    private final String email;
    private final String password;

    public AdminBootstrap(AccountService accounts,
                          @Value("${ADMIN_EMAIL:}") String email,
                          @Value("${ADMIN_PASSWORD:}") String password) {
        this.accounts = accounts;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        accounts.ensureAdmin(email, password);
    }
}
