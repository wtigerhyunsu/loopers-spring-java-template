package com.loopers.interfaces.event.payment;

import com.loopers.application.payment.PaymentCommerceService;
import com.loopers.domain.payment.PaymentCommerceEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentCommerceEventListener {
    private final PaymentCommerceService paymentCommerceService;

    @Async
    @EventListener
    public void handlePaymentRequested(PaymentCommerceEvent.Request event) {
        try {
            // payment 비지니스 로직 실행
            paymentCommerceService.processReqeustPayment(event);
        } catch (Exception e) {
            // 실제 예외 처리
        }
    }
}
