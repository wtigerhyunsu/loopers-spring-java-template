package com.loopers.application.order;

import com.loopers.application.product.ProductService;
import com.loopers.domain.product.ProductModel;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
public class OrderItemCreationService {
    private final ProductService productService;


    public OrderItemCreationService(ProductService productService) {
        this.productService = productService;
    }
    @Transactional
    public List<OrderCommand.OrderItemData> execute(OrderCommand.Create createCommand) {
        List<OrderCommand.OrderItemData> orderItems = new ArrayList<>();

        for (OrderCommand.Create.OrderItem orderItem : createCommand.productIds()) {
            ProductModel productModel =
                    productService.getProductModelById(orderItem.productId());

            productModel.decreaseStock(orderItem.quantity());


            orderItems.add(
                            OrderCommand.OrderItemData.of(
                                    productModel.getId(),
                                    orderItem.quantity(),
                                    productModel.getPrice().getValue(),
                                    productModel.getProductName().getValue(),
                                    productModel.getImgUrl().getValue()
                            )
                    );
        }
        return orderItems;
    }
}
