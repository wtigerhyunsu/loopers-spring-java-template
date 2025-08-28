package com.loopers.domain.order;

public class OrderEvent {
    public record Created(
            Long orderId,
            String orderNumber,
            Long userId,
            String cardType,
            String cardNumber
    ){
        public static Created of(
                Long orderId,
                String orderNumber,
                Long userId,
                String cardType,
                String cardNumber
                ) {
            return new Created(orderId, orderNumber, userId, cardType, cardNumber);
        }
    }
}
