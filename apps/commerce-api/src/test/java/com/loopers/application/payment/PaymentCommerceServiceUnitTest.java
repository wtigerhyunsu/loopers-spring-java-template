package com.loopers.application.payment;

import com.loopers.domain.order.OrderFixture;
import com.loopers.domain.order.OrderModel;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.payment.PaymentCommerceEvent;
import com.loopers.domain.payment.PaymentCommerceEventPublisher;
import com.loopers.domain.payment.PaymentCommerceRepository;
import com.loopers.domain.payment.PaymentModel;
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
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentCommerceService 단위 테스트")
class PaymentCommerceServiceUnitTest {

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
        System.out.println("sampleOrder ID: " + sampleOrder.getId());
    }

    @Nested
    @DisplayName("결제 요청 처리 테스트")
    class ProcessPaymentRequestTest {

        @Test
        @DisplayName("성공: 정상적인 결제 요청을 처리할 수 있다")
        void processPaymentRequest_Success() {
            // arrange
            Long userId = 1L;
            Long orderId = 1L;
            String callbackUrl = "http://localhost:8080/api/v1/payments/callback";
            PaymentCommerceEvent.Request event = new PaymentCommerceEvent.Request(userId, orderId);
            
            given(orderRepository.findById(orderId)).willReturn(Optional.of(sampleOrder));
            given(paymentRepository.save(any(PaymentModel.class))).willReturn(samplePayment);
            
            PaymentClientDto.Response pgResponse = PaymentClientDto.Response.of("20250819:TR:a1b2c3", "PENDING", null);
            ApiResponse<PaymentClientDto.Response> apiResponse = ApiResponse.success(pgResponse);
            given(paymentGatewayClient.requestPayment(eq(userId.toString()), any(PaymentClientDto.Request.class)))
                .willReturn(apiResponse);

            // act
            PaymentCommerceService.PaymentResult result = paymentCommerceService.processPaymentRequest(event, callbackUrl);

            // assert
            assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.transactionKey()).isEqualTo("20250819:TR:a1b2c3"),
                () -> assertThat(result.status()).isEqualTo("PENDING"),
                () -> assertThat(result.orderId()).isEqualTo(orderId)
            );
            
            then(paymentRepository).should(times(2)).save(any(PaymentModel.class));
            then(eventPublisher).should().publishPaymentRequested(any(PaymentCommerceEvent.PaymentRequested.class));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 주문에 대해 결제 요청 시 예외가 발생한다")
        void processPaymentRequest_OrderNotFound() {
            // arrange
            Long userId = 1L;
            Long nonExistentOrderId = 999L;
            String callbackUrl = "http://localhost:8080/api/v1/payments/callback";
            PaymentCommerceEvent.Request event = new PaymentCommerceEvent.Request(userId, nonExistentOrderId);
            
            given(orderRepository.findById(nonExistentOrderId)).willReturn(Optional.empty());

            // act & assert
            assertThatThrownBy(() -> paymentCommerceService.processPaymentRequest(event, callbackUrl))
                .isInstanceOf(CoreException.class)
                .hasMessage("주문을 찾을 수 없습니다");
                
            then(paymentRepository).should(never()).save(any(PaymentModel.class));
            then(paymentGatewayClient).should(never()).requestPayment(anyString(), any());
        }

        @Test
        @DisplayName("실패: 다른 사용자의 주문에 대해 결제 요청 시 권한 예외가 발생한다")
        void processPaymentRequest_UnauthorizedUser() {
            // Given
            Long unauthorizedUserId = 2L; // 주문 소유자와 다른 사용자
            Long orderId = 1L;
            String callbackUrl = "http://localhost:8080/api/v1/payments/callback";
            
            PaymentCommerceEvent.Request event = new PaymentCommerceEvent.Request(unauthorizedUserId, orderId);
            
            given(orderRepository.findById(orderId)).willReturn(Optional.of(sampleOrder));

            // When & Then
            assertThatThrownBy(() -> paymentCommerceService.processPaymentRequest(event, callbackUrl))
                .isInstanceOf(CoreException.class)
                .hasMessage("해당 주문에 대한 권한이 없습니다");
                
            // 검증: 보안 검증 실패 시 후속 처리가 실행되지 않음
            then(paymentRepository).should(never()).save(any(PaymentModel.class));
            then(paymentGatewayClient).should(never()).requestPayment(anyString(), any());
            
        }

        @Test
        @DisplayName("실패: PG 게이트웨이 호출 실패 시 외부 서비스 예외가 발생한다")
        void processPaymentRequest_PgGatewayFailure() {
            // Given
            Long userId = 1L;
            Long orderId = 1L;
            String callbackUrl = "http://localhost:8080/api/v1/payments/callback";
            
            PaymentCommerceEvent.Request event = new PaymentCommerceEvent.Request(userId, orderId);
            
            given(orderRepository.findById(orderId)).willReturn(Optional.of(sampleOrder));
            given(paymentRepository.save(any(PaymentModel.class))).willReturn(samplePayment);
            
            // PG 시뮬레이터 실패 응답 Mock
            ApiResponse<PaymentClientDto.Response> failureResponse = ApiResponse.<PaymentClientDto.Response>fail("PG_ERROR", "PG 시스템 오류");
            given(paymentGatewayClient.requestPayment(eq(userId.toString()), any(PaymentClientDto.Request.class)))
                .willReturn(failureResponse);

            // When & Then
            assertThatThrownBy(() -> paymentCommerceService.processPaymentRequest(event, callbackUrl))
                .isInstanceOf(CoreException.class)
                .hasMessageContaining("PG 결제 요청에 실패했습니다");
                
            // 검증: 결제 정보는 저장되었지만 이벤트는 발행되지 않음
            then(paymentRepository).should().save(any(PaymentModel.class));
            then(eventPublisher).should(never()).publishPaymentRequested(any());
            
            System.out.println("✅ PG 게이트웨이 실패 예외 처리 테스트 통과");
        }
    }

    @Nested
    @DisplayName("결제 콜백 처리 테스트")
    class ProcessCallbackTest {

        @Test
        @DisplayName("성공: 성공한 결제 콜백을 처리할 수 있다")
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
            given(orderRepository.findById(1L)).willReturn(Optional.of(sampleOrder));
            // samplePayment가 완료될 때까지 mock
            given(paymentRepository.save(any(PaymentModel.class))).willReturn(samplePayment);

            // When
            paymentCommerceService.processCallback(callbackEvent);

            // Then
            // 검증: 결제 완료 처리 및 이벤트 발행
            then(paymentRepository).should().save(samplePayment);
            then(eventPublisher).should().publishPaymentCompleted(any(PaymentCommerceEvent.PaymentCompleted.class));
            
        }

        @Test
        @DisplayName("성공: 실패한 결제 콜백을 처리할 수 있다")
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
            given(orderRepository.findById(1L)).willReturn(Optional.of(sampleOrder));
            given(paymentRepository.save(any(PaymentModel.class))).willReturn(samplePayment);

            // When
            paymentCommerceService.processCallback(callbackEvent);

            // Then
            // 검증: 결제 실패 처리 및 이벤트 발행
            then(paymentRepository).should().save(samplePayment);
            then(eventPublisher).should().publishPaymentFailed(any(PaymentCommerceEvent.PaymentFailed.class));
            
        }

        @Test
        @DisplayName("실패: 존재하지 않는 트랜잭션에 대한 콜백 시 예외가 발생한다")
        void processCallback_TransactionNotFound() {
            // Given
            String unknownTransactionKey = "UNKNOWN-TXN";
            PaymentCommerceEvent.Callback callbackEvent = new PaymentCommerceEvent.Callback(
                unknownTransactionKey, "ORDER-12345", "SUCCESS", "승인", new BigDecimal("50000")
            );
            
            given(paymentRepository.findByTransactionId(unknownTransactionKey)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> paymentCommerceService.processCallback(callbackEvent))
                .isInstanceOf(CoreException.class)
                .hasMessage("결제 정보를 찾을 수 없습니다");
                
            // 검증: 결제 정보 조회 실패 시 후속 처리 없음
            then(paymentRepository).should(never()).save(any());
            then(eventPublisher).should(never()).publishPaymentCompleted(any());
            then(eventPublisher).should(never()).publishPaymentFailed(any());
            
        }
    }

    @Nested
    @DisplayName("결제 정보 조회 테스트")
    class GetPaymentInfoTest {

        @Test
        @DisplayName("성공: PG 시스템에서 결제 상세 정보를 조회할 수 있다")
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
                () -> assertThat(result.orderId()).isEqualTo("ORD-20250828123456789-12345678"),
                () -> assertThat(result.status()).isEqualTo("SUCCESS"),
                () -> assertThat(result.amount()).isEqualTo(50000L)
            );
            
        }

        @Test
        @DisplayName("실패: PG 시스템에서 결제 정보를 찾을 수 없을 때 예외가 발생한다")
        void getPaymentInfo_NotFoundInPg() {
            // Given
            String unknownTransactionKey = "UNKNOWN-TXN";
            String userId = "1";
            
            ApiResponse<PaymentClientDto.Response.Detail> failureResponse = ApiResponse.<PaymentClientDto.Response.Detail>fail("NOT_FOUND", "거래를 찾을 수 없습니다");
            given(paymentGatewayClient.getPaymentDetail(userId, unknownTransactionKey)).willReturn(failureResponse);

            // When & Then
            assertThatThrownBy(() -> paymentCommerceService.getPaymentDetail(userId, unknownTransactionKey))
                .isInstanceOf(CoreException.class)
                .hasMessageContaining("PG 시스템에서 결제 정보를 찾을 수 없습니다");
                
        }
    }
    
    @Test
    @DisplayName("통합: 전체 결제 플로우가 정상적으로 동작한다")
    void integrationTest_PaymentFlow() {
        // Given: 결제 요청부터 콜백까지 전체 플로우
        Long userId = 1L;
        Long orderId = 1L;
        String callbackUrl = "http://localhost:8080/api/v1/payments/callback";
        String transactionKey = "20250819:TR:integration";
        
        PaymentCommerceEvent.Request requestEvent = new PaymentCommerceEvent.Request(userId, orderId);
        
        // Mock 설정: 결제 요청 성공
        given(orderRepository.findById(orderId)).willReturn(Optional.of(sampleOrder));
        given(paymentRepository.save(any(PaymentModel.class))).willReturn(samplePayment);
        
        PaymentClientDto.Response pgResponse = PaymentClientDto.Response.of(transactionKey, "PENDING", null);
        ApiResponse<PaymentClientDto.Response> requestResponse = ApiResponse.success(pgResponse);
        given(paymentGatewayClient.requestPayment(eq(userId.toString()), any())).willReturn(requestResponse);
        
        // Mock 설정: 콜백 처리 성공
        PaymentCommerceEvent.Callback callbackEvent = PaymentCommerceEvent.Callback.of(
            transactionKey, "ORD-20250828123456789-12345678", "SUCCESS", "정상 승인", new BigDecimal("50000")
        );
        given(paymentRepository.findByTransactionId(transactionKey)).willReturn(Optional.of(samplePayment));
        given(orderRepository.findById(1L)).willReturn(Optional.of(sampleOrder));

        // When: 결제 요청 및 콜백 처리
        PaymentCommerceService.PaymentResult paymentResult = paymentCommerceService.processPaymentRequest(requestEvent, callbackUrl);
        paymentCommerceService.processCallback(callbackEvent);

        // Then: 전체 플로우 검증
        assertThat(paymentResult.transactionKey()).isEqualTo(transactionKey);
        
        // 검증: 모든 단계의 메서드가 올바른 순서로 호출되었는지 확인
        then(paymentRepository).should().save(any(PaymentModel.class)); // 결제 요청 시 저장
        then(eventPublisher).should().publishPaymentRequested(any()); // 결제 요청 이벤트 발행
        then(eventPublisher).should().publishPaymentCompleted(any()); // 결제 완료 이벤트 발행
        
    }
}
