package com.loopers.application.like;

import com.loopers.domain.like.product.ProductLikeEvent;
import com.loopers.domain.like.product.ProductLikeEventPublisher;
import com.loopers.domain.like.product.ProductLikeModel;
import com.loopers.domain.like.product.ProductLikeRepository;
import com.loopers.domain.like.product.ProductLikeService;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.embeded.ProductLikeCount;
import com.loopers.interfaces.event.like.ProductLikeEventListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductLikeEventSystemTest {

    @Mock
    private ProductLikeEventPublisher eventPublisher;
    
    @Mock
    private ProductLikeRepository productLikeRepository;
    
    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    
    @Mock
    private ValueOperations<String, String> valueOperations;
    
    @Mock
    private ProductModel productModel;

    private ProductLikeService productLikeService;
    private ProductLikeEventListener eventListener;
    private ProductLikeAggregationService aggregationService;
    private ProductQueryService queryService;

    @BeforeEach
    void setUp() {
        productLikeService = new ProductLikeService(eventPublisher);
        aggregationService = new ProductLikeAggregationService(productLikeRepository, productRepository, redisTemplate);
        eventListener = new ProductLikeEventListener(redisTemplate, aggregationService);
        queryService = new ProductQueryService(redisTemplate, productRepository, productLikeRepository);
    }

    @Test
    @DisplayName("ProductLikeService should publish events when adding likes")
    void shouldPublishEventWhenAddingLike() {
        // Given
        Long userId = 1L;
        Long productId = 100L;
        when(productModel.getId()).thenReturn(productId);

        // When
        ProductLikeModel result = productLikeService.addLike(productModel, userId);

        // Then
        ArgumentCaptor<ProductLikeEvent.Added> eventCaptor = ArgumentCaptor.forClass(ProductLikeEvent.Added.class);
        verify(eventPublisher).publishLikeAdded(eventCaptor.capture());
        
        ProductLikeEvent.Added capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.userId()).isEqualTo(userId);
        assertThat(capturedEvent.productId()).isEqualTo(productId);
        assertThat(capturedEvent.timestamp()).isNotNull();
        
        verify(productModel).incrementLikeCount();
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("ProductLikeService should publish events when removing likes")
    void shouldPublishEventWhenRemovingLike() {
        // Given
        Long userId = 1L;
        Long productId = 100L;
        ProductLikeModel existingLike = mock(ProductLikeModel.class);
        when(existingLike.getUserId()).thenReturn(userId);
        when(productModel.getId()).thenReturn(productId);

        // When
        productLikeService.removeLike(productModel, existingLike);

        // Then
        ArgumentCaptor<ProductLikeEvent.Removed> eventCaptor = ArgumentCaptor.forClass(ProductLikeEvent.Removed.class);
        verify(eventPublisher).publishLikeRemoved(eventCaptor.capture());
        
        ProductLikeEvent.Removed capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.userId()).isEqualTo(userId);
        assertThat(capturedEvent.productId()).isEqualTo(productId);
        assertThat(capturedEvent.timestamp()).isNotNull();
        
        verify(productModel).decrementLikeCount();
    }

    @Test
    @DisplayName("ProductLikeEventListener should update Redis counters and schedule aggregation")
    void shouldUpdateRedisAndScheduleAggregationOnLikeAdded() {
        // Given
        Long userId = 1L;
        Long productId = 100L;
        ProductLikeEvent.Added event = ProductLikeEvent.Added.of(userId, productId);
        String expectedRedisKey = "product:like:count:" + productId;
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // When
        eventListener.handleLikeAdded(event);

        // Then
        verify(valueOperations).increment(expectedRedisKey);
    }

    @Test
    @DisplayName("ProductLikeEventListener should decrement Redis counters on like removed")
    void shouldDecrementRedisCounterOnLikeRemoved() {
        // Given
        Long userId = 1L;
        Long productId = 100L;
        ProductLikeEvent.Removed event = ProductLikeEvent.Removed.of(userId, productId);
        String expectedRedisKey = "product:like:count:" + productId;
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // When
        eventListener.handleLikeRemoved(event);

        // Then
        verify(valueOperations).decrement(expectedRedisKey);
    }

    @Test
    @DisplayName("ProductLikeAggregationService should process aggregations and update database")
    void shouldProcessAggregationsCorrectly() throws InterruptedException {
        // Given
        Long productId1 = 100L;
        Long productId2 = 200L;
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        
        aggregationService.scheduleAggregation(productId1);
        aggregationService.scheduleAggregation(productId2);
        
        // Wait for the 5 second delay requirement
        Thread.sleep(6000);
        
        Map<Long, Long> actualCounts = Map.of(
            productId1, 5L,
            productId2, 3L
        );
        
        when(productLikeRepository.countByProductIds(anyList()))
            .thenReturn(actualCounts);

        // When
        aggregationService.processAggregations();

        // Then
        verify(productLikeRepository).countByProductIds(argThat(list -> 
            list.size() == 2 && list.contains(productId1) && list.contains(productId2)));
        verify(productRepository).updateLikeCount(Map.of(productId1, 5L));
        verify(productRepository).updateLikeCount(Map.of(productId2, 3L));
        verify(valueOperations).set("product:like:count:" + productId1, "5", java.time.Duration.ofHours(1));
        verify(valueOperations).set("product:like:count:" + productId2, "3", java.time.Duration.ofHours(1));
    }

    @Test
    @DisplayName("ProductQueryService should return cached count from Redis")
    void shouldReturnCachedCountFromRedis() {
        // Given
        Long productId = 100L;
        String redisKey = "product:like:count:" + productId;
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(redisKey)).thenReturn("7");

        // When
        Long result = queryService.getLikeCount(productId);

        // Then
        assertThat(result).isEqualTo(7L);
        verify(valueOperations).get(redisKey);
        verifyNoInteractions(productRepository);
        verifyNoInteractions(productLikeRepository);
    }

    @Test
    @DisplayName("ProductQueryService should fallback to Product.likeCount when Redis fails")
    void shouldFallbackToProductLikeCountWhenRedisUnavailable() {
        // Given
        Long productId = 100L;
        String redisKey = "product:like:count:" + productId;
        ProductLikeCount likeCount = mock(ProductLikeCount.class);
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(redisKey)).thenReturn(null);
        when(productRepository.findById(productId)).thenReturn(java.util.Optional.of(productModel));
        when(productModel.getLikeCount()).thenReturn(likeCount);
        when(likeCount.getValue()).thenReturn(BigDecimal.valueOf(10));

        // When
        Long result = queryService.getLikeCount(productId);

        // Then
        assertThat(result).isEqualTo(10L);
        verify(productRepository).findById(productId);
        verify(valueOperations).set(redisKey, "10", java.time.Duration.ofHours(1));
        verifyNoInteractions(productLikeRepository);
    }

    @Test
    @DisplayName("ProductQueryService should fallback to COUNT query when all else fails")
    void shouldFallbackToCountQueryWhenAllElseFails() {
        // Given
        Long productId = 100L;
        String redisKey = "product:like:count:" + productId;
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(redisKey)).thenReturn(null);
        when(productRepository.findById(productId)).thenReturn(java.util.Optional.empty());
        when(productLikeRepository.countByProductIds(List.of(productId)))
            .thenReturn(Map.of(productId, 15L));

        // When
        Long result = queryService.getLikeCount(productId);

        // Then
        assertThat(result).isEqualTo(15L);
        verify(productLikeRepository).countByProductIds(List.of(productId));
        verify(valueOperations).set(redisKey, "15", java.time.Duration.ofMinutes(30));
    }

    @Test
    @DisplayName("ProductLikeService toggle should handle add correctly")
    void shouldToggleLikeToAdd() {
        // Given
        Long userId = 1L;
        Long productId = 100L;
        when(productModel.getId()).thenReturn(productId);

        // When - Test adding like (no existing like)
        ProductLikeService.LikeToggleResult addResult = productLikeService.toggleLike(productModel, userId, null);
        
        // Then
        assertThat(addResult.isAdded()).isTrue();
        assertThat(addResult.isRemoved()).isFalse();
        verify(eventPublisher).publishLikeAdded(any(ProductLikeEvent.Added.class));
        verify(productModel).incrementLikeCount();
    }
    
    @Test
    @DisplayName("ProductLikeService toggle should handle remove correctly")
    void shouldToggleLikeToRemove() {
        // Given
        Long userId = 1L;
        Long productId = 100L;
        
        ProductLikeModel existingLike = mock(ProductLikeModel.class);
        when(productModel.getId()).thenReturn(productId);
        
        // When - Test removing like (existing like)
        ProductLikeService.LikeToggleResult removeResult = productLikeService.toggleLike(productModel, userId, existingLike);
        
        // Then
        assertThat(removeResult.isAdded()).isFalse();
        assertThat(removeResult.isRemoved()).isTrue();
        verify(eventPublisher).publishLikeRemoved(any(ProductLikeEvent.Removed.class));
        verify(productModel).decrementLikeCount();
    }
}