package com.loopers.infrastructure.like;

import com.loopers.domain.like.product.ProductLikeEvent;
import com.loopers.domain.like.product.ProductLikeEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class ProductLikeEventPublisherImpl implements ProductLikeEventPublisher {
    
    private final ApplicationEventPublisher applicationEventPublisher;
    
    public ProductLikeEventPublisherImpl(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
    
    @Override
    public void publishLikeAdded(ProductLikeEvent.Added event) {
        applicationEventPublisher.publishEvent(event);
    }
    
    @Override
    public void publishLikeRemoved(ProductLikeEvent.Removed event) {
        applicationEventPublisher.publishEvent(event);
    }
}