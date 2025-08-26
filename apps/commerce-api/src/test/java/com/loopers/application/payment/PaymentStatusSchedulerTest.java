package com.loopers.application.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentStatusScheduler 테스트")
class PaymentStatusSchedulerTest {


    @BeforeEach
    void setUp() {

    }

    @Test
    @DisplayName("PENDING 결제 보정 스케줄러 실행")
    void correctPendingPayments_Success() {

    }

    @Test
    @DisplayName("만료 처리 스케줄러 실행")
    void expireOldPayments_Success() {

    }

}
