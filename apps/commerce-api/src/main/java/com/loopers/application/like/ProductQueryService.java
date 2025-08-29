package com.loopers.application.like;

import com.loopers.domain.like.product.ProductLikeRepository;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ProductQueryService {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final ProductRepository productRepository;
    private final ProductLikeRepository productLikeRepository;
    
    public ProductQueryService(RedisTemplate<String, String> redisTemplate,
                              ProductRepository productRepository,
                              ProductLikeRepository productLikeRepository) {
        this.redisTemplate = redisTemplate;
        this.productRepository = productRepository;
        this.productLikeRepository = productLikeRepository;
    }
    
    public Long getLikeCount(Long productId) {
        String key = "product:like:count:" + productId;
        
        String cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return Long.parseLong(cached);
        }
        
        Optional<ProductModel> product = productRepository.findById(productId);
        if (product.isPresent()) {
            Long dbCount = product.get().getLikeCount().getValue().longValue();
            
            redisTemplate.opsForValue().set(key, String.valueOf(dbCount), 
                Duration.ofHours(1));
            
            return dbCount;
        }
        
        Map<Long, Long> countMap = productLikeRepository.countByProductIds(List.of(productId));
        Long count = countMap.getOrDefault(productId, 0L);
        
        redisTemplate.opsForValue().set(key, String.valueOf(count), 
            Duration.ofMinutes(30));
        
        return count;
    }
}