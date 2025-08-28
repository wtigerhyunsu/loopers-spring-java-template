package com.loopers.domain.product;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ProductRepository {
    Page<ProductModel> search( String sort, int page, int size);
    Page<ProductModel> searchByBrandId(Long brandId, String sort, int page, int size);

    ProductModel save(ProductModel productModel);

    void deleteAll();

    Optional<ProductModel> findById(Long aLong);
    
    Optional<ProductModel> findByIdForUpdate(Long id);
    
    List<ProductModel> findByIdIn(List<Long> productIds);

    Optional<ProductModel> findByIdAndActive(Long productModelId);
    
    void updateLikeCount(Map<Long, Long> productLikeCounts);
}
