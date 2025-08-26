package com.loopers.domain.order;

import com.loopers.domain.product.ProductModel;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class OrderService {

    public OrderModel createOrder(Long userId) {
        return OrderModel.register(userId);
    }

    public BigDecimal calculatePrice(ProductModel product, int quantity) {
        BigDecimal base = product.getPrice().getValue();
        return base.multiply(new BigDecimal(quantity));
    }



    
}
