package com.loopers.application.payment;

import com.loopers.domain.payment.PaymentCommerceEvent;
import com.loopers.domain.payment.PaymentCommerceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentCommerceService {
    private final PaymentCommerceRepository paymentRepository;
    private final PgCommerceGateway pgGateway;

    public PaymentCommerceService(PaymentCommerceRepository paymentRepository,
                                  PgGateway pgGateway) {
        this.paymentRepository = paymentRepository;
        this.pgGateway = pgGateway;
    }
    // PaymentService는 결제 관련 비즈니스 로직을 처리하는 서비스입니다.

    @Transactional
    public void processReqeustPayment(PaymentCommerceEvent.Request event) {
        pgGateway
    }
}
