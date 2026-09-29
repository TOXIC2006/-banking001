package com.securebank.banking.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private final Map<String, Window> windows = new ConcurrentHashMap<>();
    private final int standardLimit;
    private final int loginLimit;
    private final long windowMillis;
    private final ObjectMapper mapper;

    public RateLimitFilter(@Value("${banking.security.rate-limit.requests}") int standardLimit,
                           @Value("${banking.security.rate-limit.login-requests}") int loginLimit,
                           @Value("${banking.security.rate-limit.window}") Duration window,
                           ObjectMapper mapper) {
        this.standardLimit = standardLimit;
        this.loginLimit = loginLimit;
        this.windowMillis = window.toMillis();
        this.mapper = mapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "OPTIONS".equals(request.getMethod()) || request.getRequestURI().startsWith("/actuator/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        boolean login = request.getRequestURI().equals("/api/auth/login");
        int limit = login ? loginLimit : standardLimit;
        String identity = request.getRemoteAddr();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            identity += ":" + authentication.getName();
        }
        String key = (login ? "login:" : "api:") + identity;
        Window current = windows.computeIfAbsent(key, ignored -> new Window(System.currentTimeMillis()));
        if (!current.allow(limit, windowMillis)) {
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("Retry-After", String.valueOf(Math.max(1, windowMillis / 1000)));
            mapper.writeValue(response.getOutputStream(), Map.of(
                    "code", "RATE_LIMITED", "message", "Too many requests; retry later"));
            return;
        }
        if (windows.size() > 10_000) {
            long staleBefore = System.currentTimeMillis() - (windowMillis * 2);
            windows.entrySet().removeIf(entry -> entry.getValue().startedAt < staleBefore);
        }
        chain.doFilter(request, response);
    }

    private static final class Window {
        private long startedAt;
        private int count;

        private Window(long startedAt) {
            this.startedAt = startedAt;
        }

        private synchronized boolean allow(int limit, long duration) {
            long now = System.currentTimeMillis();
            if (now - startedAt >= duration) {
                startedAt = now;
                count = 0;
            }
            return ++count <= limit;
        }
    }
}
