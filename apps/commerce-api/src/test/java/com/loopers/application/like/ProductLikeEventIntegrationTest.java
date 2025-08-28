package com.loopers.application.like;

import com.loopers.domain.like.product.ProductLikeEvent;
import com.loopers.domain.like.product.ProductLikeModel;
import com.loopers.domain.like.product.ProductLikeRepository;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductRepository;
import com.loopers.interfaces.event.like.ProductLikeEventListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringJUnitConfig
class ProductLikeEventIntegrationTest {

    @Mock
    private ProductLikeRepository productLikeRepository;
    
    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    
    @Mock
    private ValueOperations<String, String> valueOperations;

    private ProductLikeFacade productLikeFacade;
    private ProductLikeEventListener eventListener;
    private ProductLikeAggregationService aggregationService;
    private ProductQueryService queryService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        
        aggregationService = new ProductLikeAggregationService(productLikeRepository, productRepository, redisTemplate);
        eventListener = new ProductLikeEventListener(redisTemplate, aggregationService);
        queryService = new ProductQueryService(redisTemplate, productRepository, productLikeRepository);
    }

    @Test
    @DisplayName("Complete event flow: ProductLike add -> Event -> Redis update -> Aggregation")
    void shouldHandleCompleteEventFlowForAddLike() throws InterruptedException {
        // Given
        Long userId = 1L;
        Long productId = 100L;
        ProductLikeEvent.Added event = ProductLikeEvent.Added.of(userId, productId);
        String redisKey = "product:like:count:" + productId;
        
        // Mock aggregation service dependencies
        when(productLikeRepository.countByProductIds(anyList()))
            .thenReturn(Map.of(productId, 5L));

        // When - Handle event (simulates @TransactionalEventListener)
        eventListener.handleLikeAdded(event);
        
        // Simulate scheduled aggregation process
        aggregationService.scheduleAggregation(productId);
        // Wait for time-based requirement (5 seconds)
        Thread.sleep(5100);
        aggregationService.processAggregations();

        // Then
        // Verify Redis counter was incremented
        verify(valueOperations).increment(redisKey);
        
        // Verify aggregation was processed
        verify(productLikeRepository).countByProductIds(List.of(productId));
        verify(productRepository).updateLikeCount(Map.of(productId, 5L));
        
        // Verify Redis was synced with actual count
        verify(valueOperations).set(redisKey, "5", java.time.Duration.ofHours(1));
    }

    @Test
    @DisplayName("Complete event flow: ProductLike remove -> Event -> Redis update -> Aggregation")
    void shouldHandleCompleteEventFlowForRemoveLike() throws InterruptedException {
        // Given
        Long userId = 1L;
        Long productId = 100L;
        ProductLikeEvent.Removed event = ProductLikeEvent.Removed.of(userId, productId);
        String redisKey = "product:like:count:" + productId;
        
        // Mock aggregation service dependencies
        when(productLikeRepository.countByProductIds(anyList()))
            .thenReturn(Map.of(productId, 3L));

        // When - Handle event (simulates @TransactionalEventListener)
        eventListener.handleLikeRemoved(event);
        
        // Simulate scheduled aggregation process
        aggregationService.scheduleAggregation(productId);
        // Wait for time-based requirement (5 seconds)
        Thread.sleep(5100);
        aggregationService.processAggregations();

        // Then
        // Verify Redis counter was decremented
        verify(valueOperations).decrement(redisKey);
        
        // Verify aggregation was processed
        verify(productLikeRepository).countByProductIds(List.of(productId));
        verify(productRepository).updateLikeCount(Map.of(productId, 3L));
        
        // Verify Redis was synced with actual count
        verify(valueOperations).set(redisKey, "3", java.time.Duration.ofHours(1));
    }

    @Test
    @DisplayName("Batch aggregation should handle multiple products efficiently")
    void shouldHandleBatchAggregationEfficiently() throws InterruptedException {
        // Given
        Long productId1 = 100L;
        Long productId2 = 200L;
        Long productId3 = 300L;
        
        Map<Long, Long> expectedCounts = Map.of(
            productId1, 10L,
            productId2, 5L,
            productId3, 0L
        );
        
        when(productLikeRepository.countByProductIds(anyList()))
            .thenReturn(expectedCounts);

        // When - Schedule multiple aggregations
        aggregationService.scheduleAggregation(productId1);
        aggregationService.scheduleAggregation(productId2);
        aggregationService.scheduleAggregation(productId3);
        
        // Wait for time-based requirement (5 seconds)
        Thread.sleep(5100);
        // Process all at once
        aggregationService.processAggregations();

        // Then
        // Should make only one batch query
        verify(productLikeRepository, times(1)).countByProductIds(anyList());
        
        // Should update all products individually (implementation behavior)
        verify(productRepository).updateLikeCount(Map.of(productId1, 10L));
        verify(productRepository).updateLikeCount(Map.of(productId2, 5L));
        verify(productRepository).updateLikeCount(Map.of(productId3, 0L));
        
        // Should sync all to Redis with Duration
        verify(valueOperations).set("product:like:count:" + productId1, "10", java.time.Duration.ofHours(1));
        verify(valueOperations).set("product:like:count:" + productId2, "5", java.time.Duration.ofHours(1));
        verify(valueOperations).set("product:like:count:" + productId3, "0", java.time.Duration.ofHours(1));
    }

    @Test
    @DisplayName("Query service should handle fallback chain correctly")
    void shouldHandleFallbackChainCorrectly() {
        // Given
        Long productId = 100L;
        String redisKey = "product:like:count:" + productId;
        
        // Mock Redis cache miss
        when(valueOperations.get(redisKey)).thenReturn(null);
        
        // Mock Product.likeCount not available (product not found)
        when(productRepository.findById(productId)).thenReturn(Optional.empty());
        
        // Mock COUNT query fallback
        when(productLikeRepository.countByProductIds(List.of(productId)))
            .thenReturn(Map.of(productId, 7L));

        // When
        Long result = queryService.getLikeCount(productId);

        // Then
        assertThat(result).isEqualTo(7L);
        
        // Verify fallback chain was followed
        verify(valueOperations).get(redisKey); // 1st attempt: Redis
        verify(productRepository).findById(productId); // 2nd attempt: Product.likeCount
        verify(productLikeRepository).countByProductIds(List.of(productId)); // 3rd attempt: COUNT query
        
        // Verify result was cached in Redis
        verify(valueOperations).set(redisKey, "7", java.time.Duration.ofMinutes(30));
    }

    @Test
    @DisplayName("Error handling: Aggregation should retry on failure")
    void shouldRetryAggregationOnFailure() throws InterruptedException {
        // Given
        Long productId = 100L;
        aggregationService.scheduleAggregation(productId);
        
        // Mock first call to fail
        when(productLikeRepository.countByProductIds(anyList()))
            .thenThrow(new RuntimeException("Database temporarily unavailable"))
            .thenReturn(Map.of(productId, 8L)); // Second call succeeds

        // Wait for time delay and first attempt fails
        Thread.sleep(5100);
        aggregationService.processAggregations();
        
        // Verify aggregation was rescheduled (productId should be back in pending list)
        // Wait again for second attempt - retry tasks also need to wait 5+ seconds
        Thread.sleep(5100);
        aggregationService.processAggregations();

        // Then
        verify(productLikeRepository, times(2)).countByProductIds(anyList());
        verify(productRepository, times(1)).updateLikeCount(Map.of(productId, 8L));
    }

    @Test
    @DisplayName("Performance: Concurrent events should not interfere with aggregation")
    void shouldHandleConcurrentEventsCorrectly() throws InterruptedException {
        // Given
        Long productId = 100L;
        
        // Simulate multiple rapid events for same product
        for (int i = 0; i < 10; i++) {
            aggregationService.scheduleAggregation(productId);
        }
        
        when(productLikeRepository.countByProductIds(anyList()))
            .thenReturn(Map.of(productId, 15L));

        // Wait for time delay
        Thread.sleep(5100);
        // When
        aggregationService.processAggregations();

        // Then
        // Should process only once despite multiple scheduling calls
        verify(productLikeRepository, times(1)).countByProductIds(List.of(productId));
        verify(productRepository, times(1)).updateLikeCount(Map.of(productId, 15L));
    }
}