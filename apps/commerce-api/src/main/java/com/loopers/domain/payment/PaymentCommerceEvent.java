package com.loopers.domain.payment;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

/**
 * 결제 도메인 이벤트 클래스
 * 결제 관련 모든 이벤트를 정의합니다.
 */
public class PaymentCommerceEvent {
    
    /**
     * 결제 요청 이벤트
     * 사용자가 결제를 요청했을 때 발생
     */
    public record Request(
            Long userId,
            Long orderId
    ) {
        public static Request of(Long userId, Long orderId) {
            return new Request(userId, orderId);
        }
    }
    
    public record PaymentRequested(
            String transactionKey,
            Long orderId,
            Long userId,
            BigDecimal amount,
            String cardType,
            String cardNumber,
            ZonedDateTime requestedAt
    ) {
        public static PaymentRequested of(String transactionKey, Long orderId, Long userId, 
                                        BigDecimal amount, String cardType, String cardNumber) {
            return new PaymentRequested(transactionKey, orderId, userId, amount, cardType, cardNumber, ZonedDateTime.now());
        }
    }
    
    /**
     * PG 콜백 이벤트
     * PG 시스템에서 결제 결과를 콜백으로 통지했을 때 발생
     */
    public record Callback(
            String transactionKey,
            String orderId,
            String status,
            String reason,
            BigDecimal amount
    ) {
        public static Callback of(String transactionKey, String orderId, String status, String reason, BigDecimal amount) {
            return new Callback(transactionKey, orderId, status, reason, amount);
        }
        
        public boolean isSuccess() {
            return "SUCCESS".equals(status);
        }
        
        public boolean isFailed() {
            return "FAILED".equals(status);
        }
    }
    
    /**
     * 결제 완료 이벤트
     * 결제가 성공적으로 완료되었을 때 발생
     */
    public record PaymentCompleted(
            String transactionKey,
            Long orderId,
            Long userId,
            BigDecimal amount,
            ZonedDateTime completedAt
    ) {
        public static PaymentCompleted of(String transactionKey, Long orderId, Long userId, BigDecimal amount) {
            return new PaymentCompleted(transactionKey, orderId, userId, amount, ZonedDateTime.now());
        }
    }
    
    /**
     * 결제 실패 이벤트
     * 결제가 실패했을 때 발생
     */
    public record PaymentFailed(
            String transactionKey,
            Long orderId,
            Long userId,
            BigDecimal amount,
            String reason,
            ZonedDateTime failedAt
    ) {
        public static PaymentFailed of(String transactionKey, Long orderId, Long userId, 
                                     BigDecimal amount, String reason) {
            return new PaymentFailed(transactionKey, orderId, userId, amount, reason, ZonedDateTime.now());
        }
    }
    
    /**
     * 결제 취소 이벤트
     * 결제가 취소되었을 때 발생
     */
    public record PaymentCancelled(
            String transactionKey,
            Long orderId,
            Long userId,
            BigDecimal amount,
            String reason,
            ZonedDateTime cancelledAt
    ) {
        public static PaymentCancelled of(String transactionKey, Long orderId, Long userId, 
                                        BigDecimal amount, String reason) {
            return new PaymentCancelled(transactionKey, orderId, userId, amount, reason, ZonedDateTime.now());
        }
    }
}

