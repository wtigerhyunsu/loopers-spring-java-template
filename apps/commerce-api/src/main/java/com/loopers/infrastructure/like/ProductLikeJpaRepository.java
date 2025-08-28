package com.loopers.infrastructure.like;

import com.loopers.domain.like.product.ProductLikeModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ProductLikeJpaRepository extends JpaRepository<ProductLikeModel, Long> {
    Optional<ProductLikeModel> findByUserIdAndProductId(Long userId, Long productId);

    boolean existsByUserIdAndProductId(Long userId, Long productId);
    
    Page<ProductLikeModel> findByUserIdOrderByLikedAtDesc(Long userId, Pageable pageable);
    
    @Query("SELECT p.productId as productId, COUNT(p) as count FROM ProductLikeModel p WHERE p.productId IN :productIds GROUP BY p.productId")
    Map<Long, Long> countByProductIds(@Param("productIds") List<Long> productIds);
}
