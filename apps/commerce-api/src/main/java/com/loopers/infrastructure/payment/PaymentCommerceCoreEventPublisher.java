package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.PaymentCommerceEvent;
import com.loopers.domain.payment.PaymentCommerceEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class PaymentCommerceCoreEventPublisher implements PaymentCommerceEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    public PaymentCommerceCoreEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void paymentRequestPublisher(PaymentCommerceEvent.Request eventRequest) {
        applicationEventPublisher.publishEvent(eventRequest);
    }
}
