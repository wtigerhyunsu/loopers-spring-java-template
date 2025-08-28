package com.loopers.domain.like.product;

public interface ProductLikeEventPublisher {
    
    void publishLikeAdded(ProductLikeEvent.Added event);
    
    void publishLikeRemoved(ProductLikeEvent.Removed event);
}