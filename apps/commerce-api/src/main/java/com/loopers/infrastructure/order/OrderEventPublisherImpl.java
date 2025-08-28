package com.loopers.infrastructure.order;

import com.loopers.domain.order.OrderEvent;
import com.loopers.domain.order.OrderEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class OrderEventPublisherImpl implements OrderEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    public OrderEventPublisherImpl(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publishOrderCreated(OrderEvent.Created event) {
        applicationEventPublisher.publishEvent(event);
    }
}
