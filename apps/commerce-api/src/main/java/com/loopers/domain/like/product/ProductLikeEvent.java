package com.loopers.domain.like.product;

import java.time.Instant;

public class ProductLikeEvent {
    
    public record Added(
            Long userId,
            Long productId,
            Instant timestamp
    ) {
        public static Added of(Long userId, Long productId) {
            return new Added(userId, productId, Instant.now());
        }
    }
    
    public record Removed(
            Long userId,
            Long productId,
            Instant timestamp
    ) {
        public static Removed of(Long userId, Long productId) {
            return new Removed(userId, productId, Instant.now());
        }
    }
}