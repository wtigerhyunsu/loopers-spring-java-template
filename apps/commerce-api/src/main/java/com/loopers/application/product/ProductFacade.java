package com.loopers.application.product;

import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.product.ProductModel;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ProductFacade {
    private final BrandRepository brandRepository;
    private final ProductService productService;

    public ProductFacade(BrandRepository brandRepository, ProductService productService) {
        this.brandRepository = brandRepository;
        this.productService = productService;
    }

    @Cacheable(value = "productDetail", keyGenerator = "productKeyGenerator")
    public ProductCommand.ProductItem getProduct(Long productId) {
        if (productId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "productId는 null이면 안됩니다.");
        }

        ProductModel product = productService.getProductModelById(productId);
        BrandModel brand = getBrandModelById(product.getBrandId().getValue());

        return ProductCommand.ProductItem.of(product, brand);
    }

    @Cacheable(value = "productList", keyGenerator = "productKeyGenerator")
    public ProductCommand.ProductData getProductList(ProductCommand.Request.GetList request) {
        if (request.brandId() != null) {
            return getProductListByBrand(request);
        }
        Page<ProductModel> productPage = productService.searchProducts(request.sort(), request.page(), request.size());

        List<ProductModel> productList = productPage.getContent();
        if(productList.isEmpty()){
            return new ProductCommand.ProductData(productPage, new ArrayList<>());
        }
        List<Long> distinctBrandIds = productService.toDistinctBrandIds(productList);
        List<BrandModel> brandList = brandRepository.findByBrandIds(distinctBrandIds);

        Map<Long, BrandModel> BrandModel = productService.createBrandNameMap(brandList, distinctBrandIds);

        List<ProductCommand.ProductItem> productItems = toListWithBrands(productList, BrandModel);

        return  new ProductCommand.ProductData(productPage, productItems);
    }

    public BrandModel getBrandModelById(Long brandId) {
        return brandRepository.findById(brandId).orElseThrow(
                () -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 브랜드입니다.")
        );
    }

    private ProductCommand.ProductData getProductListByBrand(ProductCommand.Request.GetList request) {
        BrandModel brandModel = getBrandModelById(request.brandId());

        Page<ProductModel> productModels = productService.searchProductsByBrandId(
                brandModel.getId(), request.sort(), request.page(), request.size()
        );

        List<ProductCommand.ProductItem> productItems =
                toListWithSingleBrand(productModels.getContent(), brandModel);

        return new ProductCommand.ProductData(productModels, productItems);
    }
    private List<ProductCommand.ProductItem> toListWithSingleBrand(
            List<ProductModel> productModelList, BrandModel brandModel) {

        return productModelList.stream()
                .map(product ->
                        ProductCommand.ProductItem.of(product, brandModel))
                .toList();
    }

    private List<ProductCommand.ProductItem> toListWithBrands(
            List<ProductModel> productModelList, Map<Long, BrandModel> brandNameMap) {

        return productModelList.stream()
                .map(product -> {
                    Long brandId = product.getBrandId().getValue();
                    BrandModel brandMode = brandNameMap.get(brandId);

                    return ProductCommand.ProductItem.of(product, brandMode);
                })
                .toList();
    }

}
