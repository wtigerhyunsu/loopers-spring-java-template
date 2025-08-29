package com.loopers.domain.order.item;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.order.item.embeded.OrderItemPrice;
import com.loopers.domain.order.item.embeded.OrderItemProductId;
import com.loopers.domain.order.item.embeded.OrderItemQuantity;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;

import java.math.BigDecimal;

@Entity
@Table(name = "order_item")
@Getter
public class OrderItemModel extends BaseEntity {

    @Embedded
    private OrderItemProductId productId;

    @Embedded
    private OrderItemQuantity quantity;
    
    @Embedded
    private OrderItemPrice orderItemPrice;
    
    public OrderItemModel() {

    }

    private OrderItemModel(OrderItemProductId productId, OrderItemQuantity quantity, OrderItemPrice orderItemPrice) {
        this.productId = productId;
        this.quantity = quantity;
        this.orderItemPrice = orderItemPrice;
    }

    public static OrderItemModel of(Long productId, int quantity, BigDecimal pricePerUnit) {
        return new OrderItemModel(
                OrderItemProductId.of(productId),
                OrderItemQuantity.of(quantity),
                OrderItemPrice.of(pricePerUnit)
        );
    }

    public BigDecimal subtotal() {
        return this.orderItemPrice.getValue()
                .multiply(
                        new BigDecimal(this.quantity.getValue()));
    }

}
