package com.loopers.infrastructure.payment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.ZonedDateTime;

public class PaymentV1Dto {
    
    public record Request(
            @NotNull(message = "주문 ID는 필수입니다")
            String orderId,
            
            @NotNull(message = "결제 금액은 필수입니다")
            @Positive(message = "결제 금액은 양수여야 합니다")
            Long amount,
            
            @NotNull(message = "결제 수단은 필수입니다")
            String paymentMethod,
            
            Long pointUsed
    ) {
        public static Request of(String orderId, Long amount, String paymentMethod, Long pointUsed) {
            return new Request(orderId, amount, paymentMethod, pointUsed);
        }
    }
    
    public record Response(
            String transactionKey,
            String status,
            String message,
            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
            ZonedDateTime createdAt
    ) {
        public static Response of(String transactionKey, String status, String message) {
            return new Response(transactionKey, status, message, ZonedDateTime.now());
        }
        
        public boolean isSuccess() {
            return "SUCCESS".equals(status);
        }
        
        public boolean isFailed() {
            return "FAILED".equals(status);
        }
        
        public boolean isPending() {
            return "PENDING".equals(status);
        }
        
        public record Detail(
                String transactionKey,
                String orderId,
                String paymentMethod,
                String userId,
                Long amount,
                String status,
                String message,
                Long pointUsed,
                @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
                ZonedDateTime createdAt,
                @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
                ZonedDateTime updatedAt
        ) {
            public static Detail of(String transactionKey, String orderId, String paymentMethod, 
                                   String userId, Long amount, String status, String message) {
                return new Detail(transactionKey, orderId, paymentMethod, userId, amount, 
                                status, message, 0L, ZonedDateTime.now(), ZonedDateTime.now());
            }
            
            public boolean isSuccess() {
                return "SUCCESS".equals(status);
            }
            
            public boolean isFailed() {
                return "FAILED".equals(status);
            }
            
            public boolean isPending() {
                return "PENDING".equals(status);
            }
        }
    }
}