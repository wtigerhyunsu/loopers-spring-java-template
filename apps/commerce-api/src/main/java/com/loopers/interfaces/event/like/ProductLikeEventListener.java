package com.loopers.interfaces.event.like;

import com.loopers.application.like.ProductLikeAggregationService;
import com.loopers.domain.like.product.ProductLikeEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Duration;

@Component
@Slf4j
public class ProductLikeEventListener {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final ProductLikeAggregationService aggregationService;
    
    public ProductLikeEventListener(RedisTemplate<String, String> redisTemplate, 
                                   ProductLikeAggregationService aggregationService) {
        this.redisTemplate = redisTemplate;
        this.aggregationService = aggregationService;
    }
    
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    @Async
    public void handleLikeAdded(ProductLikeEvent.Added event) {
        String key = "product:like:count:" + event.productId();
        
        try {
            Long newCount = redisTemplate.opsForValue().increment(key);
            redisTemplate.expire(key, Duration.ofHours(1));
            
            aggregationService.scheduleAggregation(event.productId());
            
            log.debug("Like added - productId: {}, redisCount: {}", event.productId(), newCount);
            
        } catch (Exception e) {
            log.error("Failed to update Redis for product: {}", event.productId(), e);
            aggregationService.scheduleAggregation(event.productId());
        }
    }
    
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    @Async
    public void handleLikeRemoved(ProductLikeEvent.Removed event) {
        String key = "product:like:count:" + event.productId();
        
        try {
            Long newCount = redisTemplate.opsForValue().decrement(key);
            redisTemplate.expire(key, Duration.ofHours(1));
            
            aggregationService.scheduleAggregation(event.productId());
            
            log.debug("Like removed - productId: {}, redisCount: {}", event.productId(), newCount);
            
        } catch (Exception e) {
            log.error("Failed to update Redis for product: {}", event.productId(), e);
            aggregationService.scheduleAggregation(event.productId());
        }
    }
}