package com.loopers.application.order;

import com.loopers.application.coupon.CouponService;
import com.loopers.domain.coupon.CouponModel;
import com.loopers.domain.order.OrderEvent;
import com.loopers.domain.order.OrderModel;
import com.loopers.domain.order.OrderEventPublisher;
import com.loopers.support.error.CoreException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
@Slf4j
public class OrderFacade {
    private final OrderCreationService orderCreationService;
    private final OrderItemCreationService orderItemCreationService;
    private final CouponService couponService;
    private final OrderEventPublisher orderEventPublisher;
    private final OrderMapper orderMapper;

    public OrderFacade(OrderCreationService orderCreationService,
                       OrderItemCreationService orderItemCreationService,
                       CouponService couponService,
                       OrderEventPublisher orderEventPublisher,
                       OrderMapper orderMapper) {
        this.orderCreationService = orderCreationService;
        this.orderItemCreationService = orderItemCreationService;
        this.couponService = couponService;
        this.orderEventPublisher = orderEventPublisher;
        this.orderMapper = orderMapper;
    }
    @Transactional(rollbackFor = {Exception.class, CoreException.class})
    public OrderInfo.OrderItem createOrder(OrderCommand.Create request){
        // 1. OrderItemData를 만든다 (상품 재고 감소)
        List<OrderCommand.OrderItemData> orderItemDataList
                = orderItemCreationService.execute(request);
        //2. OrderModel을 만들어 저장한다. (OrderItemData로)
        OrderModel orderModel = orderCreationService.execute(request, orderItemDataList);
        //4. Coupon이 있으면 적용한다.
        if (request.couponId() != null) {
            CouponModel couponModel = couponService.getUserCoupons(request.couponId(), request.userId());
            BigDecimal discountAmount = couponService.applyCouponToOrder(couponModel, orderModel);

            // 주문 금액에 할인 적용
            orderModel.applyDiscount(discountAmount);  // 단순한 할인 적용 메서드
        }
        OrderEvent.Created orderCreatedEvent = OrderEvent.Created.of(
                orderModel.getId(),
                orderModel.getOrderNumber().getValue(),
                request.userId(),
                request.cardType(),
                request.cardNumber()
        );
        
        orderEventPublisher.publishOrderCreated(orderCreatedEvent);
        
        log.info("주문 생성 완료 및 이벤트 발행: orderId={}, orderNumber={}, userId={}", 
                orderModel.getId(), orderModel.getOrderNumber().getValue(), orderModel.getUserId().getValue());
        
        return orderMapper.toOrderItem(orderModel);
    }


}
