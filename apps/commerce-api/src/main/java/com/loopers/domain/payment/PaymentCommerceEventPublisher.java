package com.loopers.domain.payment;

public interface PaymentCommerceEventPublisher {

    void paymentRequestPublisher(PaymentCommerceEvent.Request eventRequest);

}

