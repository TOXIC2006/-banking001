package com.securebank.banking.notification;

import com.securebank.banking.common.BusinessException;
import com.securebank.banking.security.BankPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final OtpService otps;
    private final NotificationService notifications;
    private final BillReminderService reminders;

    public NotificationController(OtpService otps, NotificationService notifications,
                                  BillReminderService reminders) {
        this.otps = otps;
        this.notifications = notifications;
        this.reminders = reminders;
    }

    @PostMapping("/otp")
    public ResponseEntity<NotificationDtos.Accepted> requestOtp(
            @AuthenticationPrincipal BankPrincipal principal,
            @Valid @RequestBody NotificationDtos.OtpRequest request) {
        if (request.channel() == NotificationMessage.Channel.PUSH) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_OTP_CHANNEL",
                    "OTP channel must be EMAIL or SMS");
        }
        otps.request(principal.customerId(), request.channel(), request.purpose());
        return ResponseEntity.accepted().body(new NotificationDtos.Accepted("QUEUED"));
    }

    @PostMapping("/otp/verify")
    public NotificationDtos.OtpResult verifyOtp(@AuthenticationPrincipal BankPrincipal principal,
                                                 @Valid @RequestBody NotificationDtos.OtpVerification request) {
        return new NotificationDtos.OtpResult(
                otps.verify(principal.customerId(), request.purpose(), request.code()));
    }

    @GetMapping
    public List<NotificationMessage> list(@AuthenticationPrincipal BankPrincipal principal) {
        return notifications.list(principal.customerId());
    }

    @PostMapping("/reminders")
    public ResponseEntity<BillReminder> reminder(@AuthenticationPrincipal BankPrincipal principal,
                                                  @Valid @RequestBody NotificationDtos.CreateReminder request) {
        return ResponseEntity.status(201).body(reminders.create(principal.customerId(), request));
    }

    @GetMapping("/reminders")
    public List<BillReminder> reminders(@AuthenticationPrincipal BankPrincipal principal) {
        return reminders.list(principal.customerId());
    }

    @DeleteMapping("/reminders/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelReminder(@AuthenticationPrincipal BankPrincipal principal, @PathVariable String id) {
        reminders.cancel(principal.customerId(), id);
    }
}
