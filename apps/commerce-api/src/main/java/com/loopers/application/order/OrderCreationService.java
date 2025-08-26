package com.loopers.application.order;

import com.loopers.domain.order.OrderModel;
import com.loopers.domain.order.OrderRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderCreationService {
    private final OrderRepository orderRepository;

    public OrderCreationService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
    public OrderModel execute(OrderCommand.Create createCommand, List<OrderCommand.OrderItemData> itemDataList) {
        // 1. 도메인 로직으로 주문 생성
        OrderModel orderModel = OrderModel.createWithItems(createCommand.userId(), itemDataList);
        // 2. 저장
        return orderRepository.save(orderModel);
    }

}
