package com.loopers.interfaces.api.payment;

import java.math.BigDecimal;

public class PaymentV1Dto {
    public record CallbackRequest(
            String transactionKey,
            String orderId,
            String cardType,
            String cardNo,
            BigDecimal amount,
            String status,
            String reason
    ) {
        public BigDecimal amount() {
            return amount != null ? amount : BigDecimal.ZERO;
        }
    }
}
