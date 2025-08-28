package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ProductJpaRepository extends JpaRepository<ProductModel, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ProductModel p WHERE p.id = :id")
    Optional<ProductModel> findByIdForUpdate(@Param("id") Long id);
    
    List<ProductModel> findByIdIn(List<Long> productIds);

    Optional<ProductModel> findByIdAndStatus(Long productModelId, String active);
    
    @Modifying
    @Query("UPDATE ProductModel p SET p.LikeCount.productLikeCount = :count WHERE p.id = :productId")
    void updateLikeCount(@Param("productId") Long productId, @Param("count") java.math.BigDecimal count);
    
    default void updateLikeCount(Map<Long, Long> productLikeCounts) {
        productLikeCounts.forEach((productId, count) -> 
            updateLikeCount(productId, java.math.BigDecimal.valueOf(count)));
    }
}
