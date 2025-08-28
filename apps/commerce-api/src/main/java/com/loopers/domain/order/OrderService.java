package com.loopers.domain.order;

import com.loopers.domain.product.ProductModel;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
public class OrderService {
    
    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public OrderModel createOrder(Long userId) {
        return OrderModel.register(userId);
    }

    public BigDecimal calculatePrice(ProductModel product, int quantity) {
        BigDecimal base = product.getPrice().getValue();
        return base.multiply(new BigDecimal(quantity));
    }
    
    @Transactional
    public void completePayment(Long orderId) {
        OrderModel order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CoreException(ErrorType.BAD_REQUEST, "주문을 찾을 수 없습니다"));
        
        order.completePayment();
        orderRepository.save(order);
    }
    
    @Transactional
    public void failPayment(Long orderId, String reason) {
        OrderModel order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CoreException(ErrorType.BAD_REQUEST, "주문을 찾을 수 없습니다"));
        
        order.failPayment();
        orderRepository.save(order);
    }
}
