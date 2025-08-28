package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.PaymentCommerceEvent;
import com.loopers.domain.payment.PaymentCommerceEventPublisher;
import lombok.extern.log4j.Log4j;
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class PaymentEventPublisherImpl implements PaymentCommerceEventPublisher {
    

    private final ApplicationEventPublisher applicationEventPublisher;
    
    public PaymentEventPublisherImpl(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
    
    
    @Override
    public void publishPaymentRequested(PaymentCommerceEvent.PaymentRequested event) {
        log.info("Publishing payment requested event: transactionKey={}, orderId={}",
                   event.transactionKey(), event.orderId());
        applicationEventPublisher.publishEvent(event);
    }
    
    @Override
    public void publishPaymentCompleted(PaymentCommerceEvent.PaymentCompleted event) {
        log.info("Publishing payment completed event: transactionKey={}, orderId={}, amount={}",
                   event.transactionKey(), event.orderId(), event.amount());
        applicationEventPublisher.publishEvent(event);
    }
    
    @Override
    public void publishPaymentFailed(PaymentCommerceEvent.PaymentFailed event) {
        log.warn("Publishing payment failed event: transactionKey={}, orderId={}, reason={}",
                   event.transactionKey(), event.orderId(), event.reason());
        applicationEventPublisher.publishEvent(event);
    }
    
    @Override
    public void publishPaymentCancelled(PaymentCommerceEvent.PaymentCancelled event) {
        log.info("Publishing payment cancelled event: transactionKey={}, orderId={}, reason={}",
                   event.transactionKey(), event.orderId(), event.reason());
        applicationEventPublisher.publishEvent(event);
    }
}
