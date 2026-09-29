package com.securebank.banking.security;

import com.securebank.banking.common.DomainEvents;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final ApplicationEventPublisher events;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService,
                          ApplicationEventPublisher events) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.events = events;
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        BankPrincipal principal = (BankPrincipal) authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())).getPrincipal();
        events.publishEvent(new DomainEvents.LoginAlert(principal.customerId(), principal.username(),
                servletRequest.getRemoteAddr(), Instant.now()));
        return new TokenResponse(jwtService.issue(principal), "Bearer", jwtService.expiresInSeconds());
    }

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {
    }

    public record TokenResponse(String accessToken, String tokenType, long expiresInSeconds) {
    }
}
