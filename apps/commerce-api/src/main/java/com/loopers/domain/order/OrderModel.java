package com.loopers.domain.order;

import com.loopers.application.order.OrderCommand;
import com.loopers.domain.BaseEntity;
import com.loopers.domain.order.embeded.OrderNumber;
import com.loopers.domain.order.embeded.OrderStatus;
import com.loopers.domain.order.embeded.OrderTotalPrice;
import com.loopers.domain.order.embeded.OrderUserId;
import com.loopers.domain.order.item.OrderItemModel;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
public class OrderModel extends BaseEntity {
    @Embedded private OrderNumber orderNumber;
    @Embedded private OrderUserId userId;
    @Embedded private OrderStatus status;
    @Embedded private OrderTotalPrice totalPrice;

    @OneToMany(cascade = CascadeType.ALL,
            orphanRemoval = true)
    @JoinColumn(name = "order_id")
    private final List<OrderItemModel> orderItems = new ArrayList<>();


    public OrderModel() {
    }

    private OrderModel(OrderNumber orderNumber, OrderUserId userId, OrderStatus status, OrderTotalPrice totalPrice) {
        this.orderNumber = orderNumber;
        this.userId = userId;
        this.status = status;
        this.totalPrice = totalPrice;
    }

    public static OrderModel of(String orderNumber, Long userId, String status, BigDecimal totalPrice) {
        return new OrderModel(
                OrderNumber.of(orderNumber),
                OrderUserId.of(userId),
                OrderStatus.of(status),
                OrderTotalPrice.of(totalPrice)
        );
    }
    public static OrderModel createWithItems(Long userId,
                                             List<OrderCommand.OrderItemData> itemDataList
    ){
        OrderModel order = new OrderModel(
                OrderNumber.generate(userId),
                OrderUserId.of(userId),
                OrderStatus.pendingPayment(),
                OrderTotalPrice.of(BigDecimal.ZERO)
        );

        for (OrderCommand.OrderItemData itemData : itemDataList) {
            OrderItemModel item = OrderItemModel.of(
                    itemData.productId(),
                    itemData.quantity(),
                    itemData.pricePerUnit()
            );
            order.orderItems.add(item);
        }

        order.recalcTotal();
        return order;
    }
    public static OrderModel register(Long userId) {
        return new OrderModel(
                OrderNumber.generate(userId),
                OrderUserId.of(userId),
                OrderStatus.pendingPayment(),
                OrderTotalPrice.of(new BigDecimal(BigInteger.ZERO))
        );
    }

    public void addItem(Long productId, int quantity, BigDecimal pricePerUnit) {
        OrderItemModel item = OrderItemModel.of(
                productId, quantity, pricePerUnit);

        this.orderItems.add(item);
        recalcTotal();
    }
    public void replaceAllItems(List<OrderItemModel> orderItems) {
        this.orderItems.clear();
        this.orderItems.addAll(orderItems);
        recalcTotal();
    }
    public void cancel() {
        this.status = this.status.cancel();
    }
    public void updateStatus(String status) {
        this.status = this.status.updateStatus(status);
    }
    public boolean canBeCancelled() {
        return this.status.canBeCancelled();
    }
    public boolean isPendingPayment() {
        return this.status.isPendingPayment();
    }
    public boolean belongsToUser(Long userId) {
        return this.userId.getValue().equals(userId);
    }
    
    public BigDecimal calculateTotal() {
        return orderItems.stream()
                .map(OrderItemModel::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void recalcTotal() {
        this.totalPrice = OrderTotalPrice.of(calculateTotal());
    }

    public void applyDiscount(BigDecimal discountAmount) {
        if (discountAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("할인 금액은 0 이상이어야 합니다.");
        }

        BigDecimal maxDiscount = this.totalPrice.getValue();
        BigDecimal actualDiscount = discountAmount.min(maxDiscount);

        this.totalPrice = this.totalPrice.subtract(actualDiscount);
    }

}
