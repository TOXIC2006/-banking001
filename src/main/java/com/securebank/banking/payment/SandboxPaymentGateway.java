package com.securebank.banking.payment;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Deterministic local adapter. Replace this bean with provider SDK/HTTP adapters in production.
 * A beneficiary beginning with FAIL safely exercises compensation and support-ticket behavior.
 */
@Component
public class SandboxPaymentGateway implements ExternalPaymentGateway {
    @Override
    public SettlementResult settle(String reference, Payment.Rail rail, String beneficiary,
                                   String routingCode, BigDecimal amount, String currency) {
        if (beneficiary.toUpperCase().startsWith("FAIL")) {
            throw new ExternalPaymentGateway.ProviderException("External provider rejected the settlement");
        }
        return new SettlementResult("PROV-" + UUID.randomUUID().toString().substring(0, 18).toUpperCase());
    }

}
