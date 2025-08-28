package com.loopers.application.payment;

import com.loopers.domain.order.OrderModel;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.order.OrderFixture;
import com.loopers.domain.payment.*;
import com.loopers.infrastructure.payment.PaymentGatewayClient;
import com.loopers.infrastructure.payment.dto.PaymentClientDto;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentCommerceService 테스트")
class PaymentCommerceServiceTest {

    @Mock
    private PaymentCommerceRepository paymentRepository;
    
    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private PaymentGatewayClient paymentGatewayClient;
    
    @Mock
    private PaymentCommerceEventPublisher eventPublisher;

    @InjectMocks
    private PaymentCommerceService paymentCommerceService;

    private OrderModel sampleOrder;
    private PaymentModel samplePayment;

    @BeforeEach
    void setUp() {
        sampleOrder = OrderFixture.createOrderWithIdAndStatus(1L, "PENDING_PAYMENT");
        samplePayment = PaymentModel.create(1L, new BigDecimal("50000"), "CARD", BigDecimal.ZERO);
    }

    @Nested
    @DisplayName("결제 요청 처리")
    class ProcessPaymentRequestTest {

        @Test
        @DisplayName("정상적인 결제 요청을 처리할 수 있다")
        void processPaymentRequest_Success() {
            // Given
            Long userId = 1L;
            Long orderId = 1L;
            String callbackUrl = "http://localhost:8080/api/v1/payments/callback";
            
            PaymentCommerceEvent.Request event = new PaymentCommerceEvent.Request(userId, orderId);
            
            given(orderRepository.findById(orderId)).willReturn(Optional.of(sampleOrder));
            given(paymentRepository.save(any(PaymentModel.class))).willReturn(samplePayment);
            
            // PG 시뮬레이터 응답 Mock
            PaymentClientDto.Response pgResponse = PaymentClientDto.Response.of(
                "20250819:TR:a1b2c3", 
                "PENDING", 
                null
            );
            ApiResponse<PaymentClientDto.Response> apiResponse = ApiResponse.success(pgResponse);
            given(paymentGatewayClient.requestPayment(eq(userId.toString()), any(PaymentClientDto.Request.class)))
                .willReturn(apiResponse);

            // When
            PaymentCommerceService.PaymentResult result = paymentCommerceService.processPaymentRequest(event, callbackUrl);

            // Then
            assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.transactionKey()).isEqualTo("20250819:TR:a1b2c3"),
                () -> assertThat(result.status()).isEqualTo("PENDING"),
                () -> assertThat(result.orderId()).isEqualTo(orderId)
            );
            
            then(paymentRepository).should().save(any(PaymentModel.class));
            then(eventPublisher).should().publishPaymentRequested(any(PaymentCommerceEvent.PaymentRequested.class));
        }

        @Test
        @DisplayName("존재하지 않는 주문에 대해 결제 요청 시 예외가 발생한다")
        void processPaymentRequest_OrderNotFound() {
            // Given
            Long userId = 1L;
            Long orderId = 999L;
            String callbackUrl = "http://localhost:8080/api/v1/payments/callback";
            
            PaymentCommerceEvent.Request event = new PaymentCommerceEvent.Request(userId, orderId);
            
            given(orderRepository.findById(orderId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> paymentCommerceService.processPaymentRequest(event, callbackUrl))
                .isInstanceOf(CoreException.class)
                .hasMessage("주문을 찾을 수 없습니다");
                
            then(paymentRepository).should(never()).save(any(PaymentModel.class));
            then(paymentGatewayClient).should(never()).requestPayment(anyString(), any());
        }

        @Test
        @DisplayName("다른 사용자의 주문에 대해 결제 요청 시 예외가 발생한다")
        void processPaymentRequest_UnauthorizedUser() {
            // Given
            Long userId = 2L; // 다른 사용자
            Long orderId = 1L;
            String callbackUrl = "http://localhost:8080/api/v1/payments/callback";
            
            PaymentCommerceEvent.Request event = new PaymentCommerceEvent.Request(userId, orderId);
            
            given(orderRepository.findById(orderId)).willReturn(Optional.of(sampleOrder));

            // When & Then
            assertThatThrownBy(() -> paymentCommerceService.processPaymentRequest(event, callbackUrl))
                .isInstanceOf(CoreException.class)
                .hasMessage("해당 주문에 대한 권한이 없습니다");
                
            then(paymentRepository).should(never()).save(any(PaymentModel.class));
            then(paymentGatewayClient).should(never()).requestPayment(anyString(), any());
        }

        @Test
        @DisplayName("이미 결제가 완료된 주문에 대해 결제 요청 시 예외가 발생한다")
        void processPaymentRequest_AlreadyPaid() {
            // Given
            OrderModel completedOrder = OrderModel.of("ORDER-12345", 1L, "COMPLETED", new BigDecimal("50000"));
            Long userId = 1L;
            Long orderId = 1L;
            String callbackUrl = "http://localhost:8080/api/v1/payments/callback";
            
            PaymentCommerceEvent.Request event = new PaymentCommerceEvent.Request(userId, orderId);
            
            given(orderRepository.findById(orderId)).willReturn(Optional.of(completedOrder));

            // When & Then
            assertThatThrownBy(() -> paymentCommerceService.processPaymentRequest(event, callbackUrl))
                .isInstanceOf(CoreException.class)
                .hasMessage("이미 결제가 완료된 주문입니다");
                
            then(paymentRepository).should(never()).save(any(PaymentModel.class));
            then(paymentGatewayClient).should(never()).requestPayment(anyString(), any());
        }

        @Test
        @DisplayName("PG 게이트웨이 호출 실패 시 예외가 발생한다")
        void processPaymentRequest_PgGatewayFailure() {
            // Given
            Long userId = 1L;
            Long orderId = 1L;
            String callbackUrl = "http://localhost:8080/api/v1/payments/callback";
            
            PaymentCommerceEvent.Request event = new PaymentCommerceEvent.Request(userId, orderId);
            
            given(orderRepository.findById(orderId)).willReturn(Optional.of(sampleOrder));
            given(paymentRepository.save(any(PaymentModel.class))).willReturn(samplePayment);
            
            // PG 시뮬레이터 실패 응답 Mock
            ApiResponse<PaymentClientDto.Response> failureResponse = ApiResponse.fail("PG_ERROR", "PG 시스템 오류");
            given(paymentGatewayClient.requestPayment(eq(userId.toString()), any(PaymentClientDto.Request.class)))
                .willReturn(failureResponse);

            // When & Then
            assertThatThrownBy(() -> paymentCommerceService.processPaymentRequest(event, callbackUrl))
                .isInstanceOf(CoreException.class)
                .hasMessage("PG 결제 요청에 실패했습니다: PG 시스템 오류");
                
            then(paymentRepository).should().save(any(PaymentModel.class));
            then(eventPublisher).should(never()).publishPaymentRequested(any());
        }
    }

    @Nested
    @DisplayName("결제 콜백 처리")
    class ProcessCallbackTest {

        @Test
        @DisplayName("성공한 결제 콜백을 처리할 수 있다")
        void processCallback_Success() {
            // Given
            String transactionKey = "20250819:TR:a1b2c3";
            String orderId = "ORDER-12345";
            String status = "SUCCESS";
            String reason = "정상 승인되었습니다";
            BigDecimal amount = new BigDecimal("50000");
            
            PaymentCommerceEvent.Callback callbackEvent = new PaymentCommerceEvent.Callback(
                transactionKey, orderId, status, reason, amount
            );
            
            given(paymentRepository.findByTransactionId(transactionKey)).willReturn(Optional.of(samplePayment));

            // When
            paymentCommerceService.processCallback(callbackEvent);

            // Then
            assertThat(samplePayment.isCompleted()).isTrue();
            then(paymentRepository).should().save(samplePayment);
            then(eventPublisher).should().publishPaymentCompleted(any(PaymentCommerceEvent.PaymentCompleted.class));
        }

        @Test
        @DisplayName("실패한 결제 콜백을 처리할 수 있다")
        void processCallback_Failed() {
            // Given
            String transactionKey = "20250819:TR:a1b2c3";
            String orderId = "ORDER-12345";
            String status = "FAILED";
            String reason = "카드 한도 초과";
            BigDecimal amount = new BigDecimal("50000");
            
            PaymentCommerceEvent.Callback callbackEvent = new PaymentCommerceEvent.Callback(
                transactionKey, orderId, status, reason, amount
            );
            
            given(paymentRepository.findByTransactionId(transactionKey)).willReturn(Optional.of(samplePayment));

            // When
            paymentCommerceService.processCallback(callbackEvent);

            // Then
            assertThat(samplePayment.isFailed()).isTrue();
            then(paymentRepository).should().save(samplePayment);
            then(eventPublisher).should().publishPaymentFailed(any(PaymentCommerceEvent.PaymentFailed.class));
        }

        @Test
        @DisplayName("존재하지 않는 트랜잭션에 대한 콜백 시 예외가 발생한다")
        void processCallback_TransactionNotFound() {
            // Given
            String transactionKey = "UNKNOWN-TXN";
            PaymentCommerceEvent.Callback callbackEvent = new PaymentCommerceEvent.Callback(
                transactionKey, "ORDER-12345", "SUCCESS", "승인", new BigDecimal("50000")
            );
            
            given(paymentRepository.findByTransactionId(transactionKey)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> paymentCommerceService.processCallback(callbackEvent))
                .isInstanceOf(CoreException.class)
                .hasMessage("결제 정보를 찾을 수 없습니다");
                
            then(paymentRepository).should(never()).save(any());
            then(eventPublisher).should(never()).publishPaymentCompleted(any());
            then(eventPublisher).should(never()).publishPaymentFailed(any());
        }

        @Test
        @DisplayName("금액이 일치하지 않는 콜백 시 예외가 발생한다")
        void processCallback_AmountMismatch() {
            // Given
            String transactionKey = "20250819:TR:a1b2c3";
            PaymentCommerceEvent.Callback callbackEvent = new PaymentCommerceEvent.Callback(
                transactionKey, "ORDER-12345", "SUCCESS", "승인", new BigDecimal("30000") // 다른 금액
            );
            
            given(paymentRepository.findByTransactionId(transactionKey)).willReturn(Optional.of(samplePayment));

            // When & Then
            assertThatThrownBy(() -> paymentCommerceService.processCallback(callbackEvent))
                .isInstanceOf(CoreException.class)
                .hasMessage("결제 금액이 일치하지 않습니다");
                
            then(paymentRepository).should(never()).save(any());
            then(eventPublisher).should(never()).publishPaymentCompleted(any());
            then(eventPublisher).should(never()).publishPaymentFailed(any());
        }
    }

    @Nested
    @DisplayName("결제 정보 조회")
    class GetPaymentInfoTest {

        @Test
        @DisplayName("트랜잭션 키로 결제 정보를 조회할 수 있다")
        void getPaymentInfo_ByTransactionKey() {
            // Given
            String transactionKey = "20250819:TR:a1b2c3";
            String userId = "1";
            
            PaymentClientDto.Response.Detail expectedDetail = PaymentClientDto.Response.Detail.of(
                transactionKey, "ORD-20250828123456789-12345678", "CARD", userId, 50000L, "SUCCESS", "승인 완료"
            );
            ApiResponse<PaymentClientDto.Response.Detail> apiResponse = ApiResponse.success(expectedDetail);
            
            given(paymentGatewayClient.getPaymentDetail(userId, transactionKey)).willReturn(apiResponse);

            // When
            PaymentClientDto.Response.Detail result = paymentCommerceService.getPaymentDetail(userId, transactionKey);

            // Then
            assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.transactionKey()).isEqualTo(transactionKey),
                () -> assertThat(result.orderId()).isEqualTo("ORDER-12345"),
                () -> assertThat(result.status()).isEqualTo("SUCCESS")
            );
        }

        @Test
        @DisplayName("PG 시스템에서 결제 정보를 찾을 수 없을 때 예외가 발생한다")
        void getPaymentInfo_NotFoundInPg() {
            // Given
            String transactionKey = "UNKNOWN-TXN";
            String userId = "1";
            
            ApiResponse<PaymentClientDto.Response.Detail> failureResponse = ApiResponse.fail("NOT_FOUND", "거래를 찾을 수 없습니다");
            given(paymentGatewayClient.getPaymentDetail(userId, transactionKey)).willReturn(failureResponse);

            // When & Then
            assertThatThrownBy(() -> paymentCommerceService.getPaymentDetail(userId, transactionKey))
                .isInstanceOf(CoreException.class)
                .hasMessage("PG 시스템에서 결제 정보를 찾을 수 없습니다: 거래를 찾을 수 없습니다");
        }
    }
}
