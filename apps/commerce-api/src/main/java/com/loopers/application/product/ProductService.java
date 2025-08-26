package com.loopers.application.product;

import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.embeded.BrandId;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    public Page<ProductModel> searchProducts(String sort, int page, int size) {
        return productRepository.search(sort, page, size);
    }
    public Page<ProductModel> searchProductsByBrandId(Long brandId, String sort, int page, int size) {
        return productRepository.searchByBrandId(brandId, sort, page, size);
    }

    public ProductModel getProductModelById(Long productModelId){
        ProductModel productModel = productRepository.findById(productModelId).orElseThrow(
                () -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 상품입니다."));

        if(!productModel.isAvailable()){
            throw new CoreException(ErrorType.BAD_REQUEST, "판매 중지된 상품입니다.");
        }
        return productModel;
    }
    public ProductModel getProductModelByIdAndActive(Long productModelId){
        return productRepository.findByIdAndActive(productModelId).orElseThrow(
                () -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 상품입니다."));
    }
// =======================================ProductDomainService==================================================
    public List<Long> toDistinctBrandIds(List<ProductModel> content) {
        return content.stream().map(ProductModel::getBrandId)
                .map(BrandId::getValue)
                .distinct()
                .toList();
    }
    public Map<Long, BrandModel> createBrandNameMap(List<BrandModel> brandModels, List<Long> requestedBrandIds) {
        if (brandModels.size() != requestedBrandIds.size()) {
            throw new CoreException(ErrorType.INTERNAL_ERROR, "상품에 포함된 브랜드 정보가 존재하지 않습니다.");
        }

        return brandModels.stream().collect(Collectors.toMap(
                BrandModel::getId,
                brandModel -> brandModel
        ));
    }

}
