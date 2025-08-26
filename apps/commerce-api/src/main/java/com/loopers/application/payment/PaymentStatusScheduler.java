package com.loopers.application.payment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PaymentStatusScheduler {
    /*
        Pg연동에 필요한 스케줄러를 구현 예정
    */
    /**
     * 1분마다 PENDING 상태 결제 보정
     */
    @Scheduled(fixedDelay = 60000) // 1분
    public void correctPendingPayments() {

    }
}
