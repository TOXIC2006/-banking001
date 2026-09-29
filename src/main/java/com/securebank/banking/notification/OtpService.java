package com.securebank.banking.notification;

import com.securebank.banking.account.AccountDtos;
import com.securebank.banking.account.AccountService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {
    private final Map<String, PendingOtp> pending = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    private final AccountService accounts;
    private final ApplicationEventPublisher events;

    public OtpService(AccountService accounts, ApplicationEventPublisher events) {
        this.accounts = accounts;
        this.events = events;
    }

    public void request(Long customerId, NotificationMessage.Channel channel, String purpose) {
        AccountDtos.AccountView account = accounts.get(customerId);
        String destination = channel == NotificationMessage.Channel.SMS ? account.phone() : account.email();
        String code = String.format("%06d", random.nextInt(1_000_000));
        pending.put(key(customerId, purpose), new PendingOtp(hash(code), Instant.now().plus(5, ChronoUnit.MINUTES)));
        events.publishEvent(new NotificationEvents.OtpRequested(customerId, channel, destination, purpose, code));
    }

    public boolean verify(Long customerId, String purpose, String code) {
        String key = key(customerId, purpose);
        PendingOtp otp = pending.get(key);
        if (otp == null || otp.expiresAt().isBefore(Instant.now())) {
            pending.remove(key);
            return false;
        }
        boolean valid = MessageDigest.isEqual(otp.hash().getBytes(StandardCharsets.UTF_8),
                hash(code).getBytes(StandardCharsets.UTF_8));
        if (valid) pending.remove(key);
        return valid;
    }

    private String key(Long customerId, String purpose) {
        return customerId + ":" + purpose.toUpperCase();
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }

    private record PendingOtp(String hash, Instant expiresAt) {
    }
}
