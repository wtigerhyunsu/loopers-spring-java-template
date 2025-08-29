package com.loopers.infrastructure.like;

import com.loopers.domain.like.product.ProductLikeEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class ProductLikeEventPublisher implements com.loopers.domain.like.product.ProductLikeEventPublisher {
    
    private final ApplicationEventPublisher applicationEventPublisher;
    
    public ProductLikeEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
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