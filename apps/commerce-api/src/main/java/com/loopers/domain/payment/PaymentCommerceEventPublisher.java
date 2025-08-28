package com.loopers.domain.payment;

public interface PaymentCommerceEventPublisher {
    

    void publishPaymentRequested(PaymentCommerceEvent.PaymentRequested event);
    
    /**
     * 결제 완료 이벤트를 발행합니다.
     *
     * @param event 결제가 성공적으로 완료되었을 때 발생하는 이벤트
     */
    void publishPaymentCompleted(PaymentCommerceEvent.PaymentCompleted event);
    
    /**
     * 결제 실패 이벤트를 발행합니다.
     * 
     * @param event 결제가 실패했을 때 발생하는 이벤트
     */
    void publishPaymentFailed(PaymentCommerceEvent.PaymentFailed event);
    
    /**
     * 결제 취소 이벤트를 발행합니다.
     * 
     * @param event 결제가 취소되었을 때 발생하는 이벤트
     */
    void publishPaymentCancelled(PaymentCommerceEvent.PaymentCancelled event);
}

