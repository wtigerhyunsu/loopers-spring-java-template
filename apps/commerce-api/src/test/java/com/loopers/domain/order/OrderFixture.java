package com.loopers.domain.order;

import java.math.BigDecimal;

public class OrderFixture {

    public static final String ORDER_NUMBER = "ORD-20250807213015999-1A2B3C4D";
    public static final Long ORDER_USER_ID = 1L;
    public static final Long ORDER_PRODUCT_ID = 1L;
    public static final int ORDER_QUANTITY = 2;
    public static final BigDecimal ORDER_PRICE_PER_UNIT = new BigDecimal("10000");
    public static final String ORDER_ORDER_STATUS = "PAYMENT_COMPLETED";

    public static OrderModel createOrderModel() {
        OrderModel order = OrderModel.of(ORDER_NUMBER, ORDER_USER_ID, ORDER_ORDER_STATUS, BigDecimal.ZERO);
        order.addItem(ORDER_PRODUCT_ID, ORDER_QUANTITY, ORDER_PRICE_PER_UNIT);
        return order;
    }

    public static OrderModel createOrderWithStatus(String status) {
        OrderModel order = OrderModel.of(ORDER_NUMBER, ORDER_USER_ID, status, BigDecimal.ZERO);
        order.addItem(ORDER_PRODUCT_ID, ORDER_QUANTITY, ORDER_PRICE_PER_UNIT);
        return order;
    }

    public static OrderModel createOrderWithUserId(Long userId) {
        OrderModel order = OrderModel.of(ORDER_NUMBER, userId, ORDER_ORDER_STATUS, BigDecimal.ZERO);
        order.addItem(ORDER_PRODUCT_ID, ORDER_QUANTITY, ORDER_PRICE_PER_UNIT);
        return order;
    }
    
    public static OrderModel createOrderWithOrderNumber(String orderNumber) {
        OrderModel order = OrderModel.of(orderNumber, ORDER_USER_ID, ORDER_ORDER_STATUS, BigDecimal.ZERO);
        order.addItem(ORDER_PRODUCT_ID, ORDER_QUANTITY, ORDER_PRICE_PER_UNIT);
        return order;
    }

    public static OrderModel createOrderWithItem(Long productId, int quantity, BigDecimal price) {
        OrderModel order = OrderModel.of(ORDER_NUMBER, ORDER_USER_ID, ORDER_ORDER_STATUS, BigDecimal.ZERO);
        order.addItem(productId, quantity, price);
        return order;
    }
    
    public static OrderModel createOrderWithOrderPrice(BigDecimal price) {
        OrderModel order = OrderModel.of(ORDER_NUMBER, ORDER_USER_ID, ORDER_ORDER_STATUS, price);
        order.addItem(ORDER_PRODUCT_ID, ORDER_QUANTITY, ORDER_PRICE_PER_UNIT);
        return order;
    }
    
    public static OrderModel createOrderWithOrderStatus(String status) {
        OrderModel order = OrderModel.of(ORDER_NUMBER, ORDER_USER_ID, status, BigDecimal.ZERO);
        order.addItem(ORDER_PRODUCT_ID, ORDER_QUANTITY, ORDER_PRICE_PER_UNIT);
        return order;
    }
}
