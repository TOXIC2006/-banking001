package com.securebank.banking.payment;

import java.math.BigDecimal;

/** Adapter seam for a real bank switch, utility aggregator, or payment gateway SDK/API. */
public interface ExternalPaymentGateway {
    SettlementResult settle(String reference, Payment.Rail rail, String beneficiary,
                            String routingCode, BigDecimal amount, String currency);

    record SettlementResult(String providerReference) {
    }

    class ProviderException extends RuntimeException {
        public ProviderException(String message) {
            super(message);
        }
    }
}
