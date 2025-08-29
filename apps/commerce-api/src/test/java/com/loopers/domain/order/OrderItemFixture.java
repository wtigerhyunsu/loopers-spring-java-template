package com.loopers.domain.order;

import com.loopers.domain.order.item.OrderItemModel;

import java.math.BigDecimal;

public class OrderItemFixture {

    public static final Long ORDER_ITEM_PRODUCT_ID = 1L;
    public static final int ORDER_ITEM_QUANTITY = 2;
    public static final BigDecimal ORDER_ITEM_PRICE_PER_UNIT = new BigDecimal("10000");

    /**
     * 기본 OrderItemModel 생성
     */
    public static OrderItemModel createOrderItem(OrderModel orderModel) {
        return createOrderItem(
                orderModel,
                ORDER_ITEM_PRODUCT_ID,
                ORDER_ITEM_QUANTITY,
                ORDER_ITEM_PRICE_PER_UNIT
        );
    }

    /**
     * 커스텀 값으로 OrderItemModel 생성
     */
    public static OrderItemModel createOrderItem(
            OrderModel orderModel,
            Long productId,
            int quantity,
            BigDecimal pricePerUnit
    ) {
        OrderItemModel item = OrderItemModel.of(
                productId,
                quantity,
                pricePerUnit
        );
        return item;
    }

    public static OrderItemModel createWithOptionName(OrderModel orderModel, String optionName) {
        return createOrderItem(orderModel,
                ORDER_ITEM_PRODUCT_ID,
                ORDER_ITEM_QUANTITY,
                ORDER_ITEM_PRICE_PER_UNIT
        );
    }

    public static OrderItemModel createWithImageUrl(OrderModel orderModel, String imageUrl) {
        return createOrderItem(orderModel,
                ORDER_ITEM_PRODUCT_ID,
                ORDER_ITEM_QUANTITY,
                ORDER_ITEM_PRICE_PER_UNIT
        );
    }

    public static OrderItemModel createWithPrice(OrderModel orderModel, BigDecimal price) {
        return createOrderItem(orderModel,
                ORDER_ITEM_PRODUCT_ID,
                ORDER_ITEM_QUANTITY,
                price
        );
    }

    public static OrderItemModel createWithQuantity(OrderModel orderModel, int quantity) {
        return createOrderItem(orderModel,
                ORDER_ITEM_PRODUCT_ID,
                quantity,
                ORDER_ITEM_PRICE_PER_UNIT
        );
    }

    public static OrderItemModel createWithProductId(OrderModel orderModel, Long productId) {
        return createOrderItem(
                orderModel,
                productId,
                ORDER_ITEM_QUANTITY,
                ORDER_ITEM_PRICE_PER_UNIT
        );
    }

    public static OrderItemModel createWithOptionId(OrderModel orderModel, Long optionId) {
        // optionId는 더 이상 사용되지 않으므로 기본 값으로 생성
        return createOrderItem(orderModel,
                ORDER_ITEM_PRODUCT_ID,
                ORDER_ITEM_QUANTITY,
                ORDER_ITEM_PRICE_PER_UNIT
        );
    }

    public static OrderItemModel createWithProductName(OrderModel orderModel, String productName) {
        // productName은 더 이상 코어 도메인에서 관리하지 않으므로 기본 값으로 생성
        return createOrderItem(orderModel,
                ORDER_ITEM_PRODUCT_ID,
                ORDER_ITEM_QUANTITY,
                ORDER_ITEM_PRICE_PER_UNIT
        );
    }

    /**
     * 테스트에서 사용하는 3개 파라미터 오버로드 메서드 (OrderModel, quantity, pricePerUnit)
     */
    public static OrderItemModel createOrderItem(
            OrderModel orderModel,
            int quantity,
            BigDecimal pricePerUnit
    ) {
        return OrderItemModel.of(
                ORDER_ITEM_PRODUCT_ID,
                quantity,
                pricePerUnit
        );
    }

    /**
     * 테스트에서 사용하는 5개 파라미터 오버로드 메서드 (OrderModel, productId, optionId, quantity, pricePerUnit)
     */
    public static OrderItemModel createOrderItem(
            OrderModel orderModel,
            Long productId,
            Long optionId,
            int quantity,
            BigDecimal pricePerUnit
    ) {
        // optionId는 더 이상 사용되지 않으므로 무시하고 기본 생성
        return OrderItemModel.of(
                productId,
                quantity,
                pricePerUnit
        );
    }
}
