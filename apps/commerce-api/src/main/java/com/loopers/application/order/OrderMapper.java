package com.loopers.application.order;

import com.loopers.domain.order.OrderModel;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {
    
    public OrderInfo.OrderItem toOrderItem(OrderModel orderModel) {
        return new OrderInfo.OrderItem(
                orderModel.getId(),
                orderModel.getOrderNumber().getValue(),
                orderModel.getUserId().getValue(),
                orderModel.getStatus().getValue(),
                orderModel.getTotalPrice().getValue(),
                orderModel.getCreatedAt().toLocalDateTime()
        );
    }
}
