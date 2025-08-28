package com.loopers.application.like;

import com.loopers.domain.like.product.ProductLikeRepository;
import com.loopers.domain.product.ProductRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ProductLikeAggregationService {
    
    private final Map<Long, AggregationTask> pendingTasks = new ConcurrentHashMap<>();
    private final ProductLikeRepository productLikeRepository;
    private final ProductRepository productRepository;
    private final RedisTemplate<String, String> redisTemplate;
    
    @Data
    private static class AggregationTask {
        private final Long productId;
        private Instant scheduledTime;
        private int retryCount = 0;
        
        public AggregationTask(Long productId, Instant scheduledTime) {
            this.productId = productId;
            this.scheduledTime = scheduledTime;
        }
    }
    
    public ProductLikeAggregationService(ProductLikeRepository productLikeRepository, 
                                        ProductRepository productRepository,
                                        RedisTemplate<String, String> redisTemplate) {
        this.productLikeRepository = productLikeRepository;
        this.productRepository = productRepository;
        this.redisTemplate = redisTemplate;
    }
    
    public void scheduleAggregation(Long productId) {
        pendingTasks.put(productId, new AggregationTask(productId, Instant.now()));
    }
    
    @Scheduled(fixedDelay = 5000)
    public void processScheduledAggregations() {
        processAggregations();
    }
    
    public void processAggregations() {
        if (pendingTasks.isEmpty()) {
            return;
        }
        
        Instant cutoff = Instant.now().minusSeconds(5);
        List<AggregationTask> tasksToProcess = pendingTasks.values().stream()
            .filter(task -> task.getScheduledTime().isBefore(cutoff))
            .toList();
        
        if (tasksToProcess.isEmpty()) {
            return;
        }
        
        log.info("Processing {} aggregation tasks", tasksToProcess.size());
        
        try {
            Map<Long, Long> counts = batchCountLikes(
                tasksToProcess.stream().map(AggregationTask::getProductId).toList()
            );
            
            for (AggregationTask task : tasksToProcess) {
                try {
                    Long actualCount = counts.get(task.getProductId());
                    if (actualCount != null) {
                        updateProductCount(task.getProductId(), actualCount);
                    }
                    
                    pendingTasks.remove(task.getProductId());
                    
                } catch (Exception e) {
                    handleAggregationError(task, e);
                }
            }
        } catch (Exception e) {
            // Handle batch query failure - reschedule all tasks
            log.error("Batch count query failed, rescheduling all tasks", e);
            for (AggregationTask task : tasksToProcess) {
                handleAggregationError(task, e);
            }
        }
    }
    
    private Map<Long, Long> batchCountLikes(List<Long> productIds) {
        return productLikeRepository.countByProductIds(productIds);
    }
    
    @Transactional
    private void updateProductCount(Long productId, Long count) {
        productRepository.updateLikeCount(Map.of(productId, count));
        
        String key = "product:like:count:" + productId;
        redisTemplate.opsForValue().set(key, String.valueOf(count), 
            Duration.ofHours(1));
        
        log.debug("Updated product {} like count to {}", productId, count);
    }
    
    private void handleAggregationError(AggregationTask task, Exception e) {
        task.setRetryCount(task.getRetryCount() + 1);
        
        if (task.getRetryCount() >= 3) {
            log.error("Failed to aggregate product {} after 3 attempts", 
                     task.getProductId(), e);
            pendingTasks.remove(task.getProductId());
        } else {
            task.setScheduledTime(Instant.now());
            log.warn("Aggregation failed for product {}, retry {}/3", 
                    task.getProductId(), task.getRetryCount());
        }
    }
}