package com.loopers.application.order;

import java.math.BigDecimal;
import java.util.List;

public class OrderCommand {
    public record Create(
            Long userId,
            List<OrderItem> productIds,
            Long couponId,
            String payType,
            String cardNumber
    ) {
        public record OrderItem(
                Long productId,
                int quantity
        ) {}

    }
    public record GetList(
            Long userId,
            String status,
            int page,
            int size
    ) {
    }
    public record GetDetail(
            Long orderId,
            Long userId
    ) {
    }
    public record OrderItemData(
            Long productId,
            int quantity,
            BigDecimal pricePerUnit,
            String productName,
            String imageUrl
    ){
        public static OrderItemData of(Long productId, int quantity,
                                       BigDecimal pricePerUnit, String productName, String imageUrl) {
            return new OrderItemData(productId,
                    quantity,
                    pricePerUnit,
                    productName,
                    imageUrl
            );
        }
    }
}
