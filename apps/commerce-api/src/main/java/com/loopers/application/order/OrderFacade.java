package com.loopers.application.order;

import com.loopers.application.coupon.CouponService;
import com.loopers.domain.coupon.CouponModel;
import com.loopers.domain.order.OrderModel;
import com.loopers.domain.payment.PaymentCommerceEventPublisher;
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
    private final PaymentCommerceEventPublisher paymentEventPublisher;

    public OrderFacade(OrderCreationService orderCreationService,
                       OrderItemCreationService orderItemCreationService,
                       CouponService couponService,
                       PaymentCommerceEventPublisher paymentEventPublisher) {
        this.orderCreationService = orderCreationService;
        this.orderItemCreationService = orderItemCreationService;
        this.couponService = couponService;
        this.paymentEventPublisher = paymentEventPublisher;
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

//        paymentEventPublisher.paymentPublisher();
        return null;
        //5. 결제처리한다.(event)
    }


}
