package com.loopers.domain.order;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;

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
    
    /**
     * 테스트용으로 OrderModel에 ID를 설정합니다.
     */
    public static OrderModel createOrderWithId(Long id) {
        OrderModel order = createOrderWithStatus("PENDING_PAYMENT");
        setId(order, id);
        return order;
    }
    
    /**
     * 테스트용으로 OrderModel에 ID를 설정합니다.
     */
    public static OrderModel createOrderWithIdAndStatus(Long id, String status) {
        OrderModel order = createOrderWithCardInfo(status, "SAMSUNG", "4111-1111-1111-1111");
        setId(order, id);
        return order;
    }
    
    /**
     * 카드 정보를 포함한 OrderModel을 생성합니다.
     */
    public static OrderModel createOrderWithCardInfo(String status, String cardType, String cardNumber) {
        OrderModel order = OrderModel.createWithItems(
            ORDER_USER_ID,
            List.of(com.loopers.application.order.OrderCommand.OrderItemData.of(
                ORDER_PRODUCT_ID, 
                ORDER_QUANTITY, 
                ORDER_PRICE_PER_UNIT, 
                "Test Product", 
                "test-image.jpg"
            )),
            cardType,
            cardNumber
        );
        order.updateStatus(status);
        return order;
    }
    
    /**
     * 테스트용으로 OrderModel에 사용자 ID와 Order ID를 설정합니다.
     */
    public static OrderModel createOrderWithIdAndUserId(Long id, Long userId) {
        OrderModel order = createOrderWithUserId(userId);
        setId(order, id);
        return order;
    }
    
    /**
     * Reflection을 사용해서 BaseEntity의 ID를 설정합니다.
     */
    private static void setId(OrderModel order, Long id) {
        try {
            Field idField = order.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(order, id);
        } catch (Exception e) {
            throw new RuntimeException("테스트용 ID 설정 실패", e);
        }
    }
}
