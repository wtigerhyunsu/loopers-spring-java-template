package com.loopers.interfaces.event.oreder;

import com.loopers.application.payment.PaymentCommerceService;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.PaymentCommerceEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventListener {
    private final PaymentCommerceService paymentCommerceService;
    private final OrderService orderService;

    @Async
    @EventListener
    public void handlePaymentRequested(PaymentCommerceEvent.Request event) {
        try {
            paymentCommerceService.processPaymentRequest(event, "http://localhost:8080/payment/callback");
        } catch (Exception e) {
            // 실제 예외 처리
        }
    }
    
    @Async
    @EventListener
    public void handlePaymentCompleted(PaymentCommerceEvent.PaymentCompleted event) {
        try {
            orderService.completePayment(event.orderId());
        } catch (Exception e) {
            // 결제 완료 후 주문 상태 변경 실패 처리
        }
    }
    
    @Async
    @EventListener
    public void handlePaymentFailed(PaymentCommerceEvent.PaymentFailed event) {
        try {
            orderService.failPayment(event.orderId(), event.reason());
        } catch (Exception e) {
            // 결제 실패 후 주문 상태 변경 실패 처리
        }
    }
}
