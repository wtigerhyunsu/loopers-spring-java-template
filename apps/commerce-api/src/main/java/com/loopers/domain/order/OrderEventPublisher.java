package com.loopers.domain.order;

public interface OrderEventPublisher {
    
    void publishOrderCreated(OrderEvent.Created event);
}
