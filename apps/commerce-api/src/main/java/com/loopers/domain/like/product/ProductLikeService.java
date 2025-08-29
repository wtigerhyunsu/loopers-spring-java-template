package com.loopers.domain.like.product;

import com.loopers.domain.product.ProductModel;
import org.springframework.stereotype.Service;

@Service
public class ProductLikeService {
    
    private final ProductLikeEventPublisher eventPublisher;
    
    public ProductLikeService(ProductLikeEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
    
    public ProductLikeModel addLike(ProductModel product, Long userId) {
        var newLike = ProductLikeModel.create(userId, product.getId());
        product.incrementLikeCount();
        eventPublisher.publishLikeAdded(ProductLikeEvent.Added.of(userId, product.getId()));
        return newLike;
    }
    
    public void removeLike(ProductModel product, ProductLikeModel existingLike) {
        product.decrementLikeCount();
        eventPublisher.publishLikeRemoved(ProductLikeEvent.Removed.of(existingLike.getUserId(), product.getId()));
    }
    
    public LikeToggleResult toggleLike(ProductModel product, Long userId, ProductLikeModel existingLike) {
        if (existingLike != null) {
            product.decrementLikeCount();
            eventPublisher.publishLikeRemoved(ProductLikeEvent.Removed.of(userId, product.getId()));
            return LikeToggleResult.removed(existingLike);
        } else {
            var newLike = ProductLikeModel.create(userId, product.getId());
            product.incrementLikeCount();
            eventPublisher.publishLikeAdded(ProductLikeEvent.Added.of(userId, product.getId()));
            return LikeToggleResult.added(newLike);
        }
    }
    
    public static class LikeToggleResult {
        private final ProductLikeModel like;
        private final boolean isAdded;
        
        private LikeToggleResult(ProductLikeModel like, boolean isAdded) {
            this.like = like;
            this.isAdded = isAdded;
        }
        
        public static LikeToggleResult added(ProductLikeModel like) {
            return new LikeToggleResult(like, true);
        }
        
        public static LikeToggleResult removed(ProductLikeModel like) {
            return new LikeToggleResult(like, false);
        }
        
        public ProductLikeModel getLike() {
            return like;
        }
        
        public boolean isAdded() {
            return isAdded;
        }
        
        public boolean isRemoved() {
            return !isAdded;
        }
    }
}
