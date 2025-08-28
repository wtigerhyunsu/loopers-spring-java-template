package com.loopers.application.payment;

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
import com.loopers.support.error.ErrorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 결제 도메인 애플리케이션 서비스
 * 
 * DDD 아키텍처에서 결제와 관련된 비즈니스 유스케이스를 조율합니다.
 * 도메인 모델과 인프라스트럭처 계층을 연결하는 역할을 합니다.
 */
@Service
public class PaymentCommerceService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentCommerceService.class);
    
    private final PaymentCommerceRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentGatewayClient paymentGatewayClient;
    private final PaymentCommerceEventPublisher eventPublisher;

    public PaymentCommerceService(
            PaymentCommerceRepository paymentRepository,
            OrderRepository orderRepository,
            PaymentGatewayClient paymentGatewayClient,
            PaymentCommerceEventPublisher eventPublisher
    ) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.paymentGatewayClient = paymentGatewayClient;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public PaymentResult processPaymentRequest(PaymentCommerceEvent.Request event, String callbackUrl) {
        logger.info("Starting payment request processing for userId={}, orderId={}", event.userId(), event.orderId());
        
        // 1. 주문 정보 검증
        OrderModel order = validateAndGetOrder(event.userId(), event.orderId());
        
        // 2. 결제 모델 생성
        PaymentModel payment = createPaymentModel(order);
        PaymentModel savedPayment = paymentRepository.save(payment);
        
        // 3. PG 시스템에 결제 요청
        PaymentClientDto.Response pgResponse = requestPaymentToPg(event.userId(), order, callbackUrl);
        
        // 4. 트랜잭션 키로 결제 정보 업데이트
        savedPayment.complete(pgResponse.transactionKey());
        paymentRepository.save(savedPayment);
        
        // 5. 결제 요청 완료 이벤트 발행
        PaymentCommerceEvent.PaymentRequested paymentRequestedEvent = PaymentCommerceEvent.PaymentRequested.of(
            pgResponse.transactionKey(),
            event.orderId(),
            event.userId(),
            order.calculateTotal(),
            order.getCardType(),
            order.getCardNumber()
        );
        eventPublisher.publishPaymentRequested(paymentRequestedEvent);
        
        logger.info("Payment request completed successfully. transactionKey={}", pgResponse.transactionKey());
        
        return PaymentResult.of(pgResponse.transactionKey(), pgResponse.status(), event.orderId());
    }

    @Transactional
    public void processCallback(PaymentCommerceEvent.Callback callbackEvent) {
        logger.info("Processing payment callback for transactionKey={}, status={}", 
                   callbackEvent.transactionKey(), callbackEvent.status());
        
        PaymentModel payment = paymentRepository.findByTransactionId(callbackEvent.transactionKey())
                .orElseThrow(() -> {
                    logger.error("Payment not found for transactionKey={}", callbackEvent.transactionKey());
                    throw new CoreException(ErrorType.BAD_REQUEST, "결제 정보를 찾을 수 없습니다");
                });
        
        if (!payment.validateCallback(callbackEvent.amount(), callbackEvent.orderId())) {
            throw new CoreException(ErrorType.BAD_REQUEST, "결제 금액이 일치하지 않습니다");
        }
        
        if (callbackEvent.isSuccess()) {
            payment.complete(callbackEvent.transactionKey());
            paymentRepository.save(payment);
            
            PaymentCommerceEvent.PaymentCompleted completedEvent = PaymentCommerceEvent.PaymentCompleted.of(
                callbackEvent.transactionKey(),
                payment.getOrderIdValue(),
                getUserIdFromPayment(payment),
                payment.getAmountValue()
            );
            eventPublisher.publishPaymentCompleted(completedEvent);
            
            logger.info("Payment completed successfully. transactionKey={}", callbackEvent.transactionKey());
            
        } else if (callbackEvent.isFailed()) {
            payment.fail(callbackEvent.reason());
            paymentRepository.save(payment);
            
            PaymentCommerceEvent.PaymentFailed failedEvent = PaymentCommerceEvent.PaymentFailed.of(
                callbackEvent.transactionKey(),
                payment.getOrderIdValue(),
                getUserIdFromPayment(payment),
                payment.getAmountValue(),
                callbackEvent.reason()
            );
            eventPublisher.publishPaymentFailed(failedEvent);
            
            logger.warn("Payment failed. transactionKey={}, reason={}", 
                       callbackEvent.transactionKey(), callbackEvent.reason());
        }
    }

    @Transactional
    public PaymentClientDto.Response.Detail getPaymentDetail(String userId, String transactionKey) {
        logger.debug("Getting payment detail for userId={}, transactionKey={}", userId, transactionKey);
        
        ApiResponse<PaymentClientDto.Response.Detail> response = paymentGatewayClient.getPaymentDetail(userId, transactionKey);
        
        if (response.meta().result() != ApiResponse.Metadata.Result.SUCCESS) {
            throw new CoreException(ErrorType.BAD_REQUEST, 
                                   "PG 시스템에서 결제 정보를 찾을 수 없습니다: " + response.meta().message());
        }
        
        PaymentClientDto.Response.Detail paymentDetail = response.data();
        
        // 결제 상태에 따른 이벤트 발행
        publishEventBasedOnPaymentStatus(userId, transactionKey, paymentDetail);
        
        return paymentDetail;
    }

    private OrderModel validateAndGetOrder(Long userId, Long orderId) {
        OrderModel order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CoreException(ErrorType.BAD_REQUEST, "주문을 찾을 수 없습니다"));
        
        if (!order.belongsToUser(userId)) {
            throw new CoreException(ErrorType.FORBIDDEN, "해당 주문에 대한 권한이 없습니다");
        }
        
        if (!order.isPendingPayment()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이미 결제가 완료된 주문입니다");
        }
        
        return order;
    }
    
    private PaymentModel createPaymentModel(OrderModel order) {
        return PaymentModel.create(
            order.getId(),
            order.calculateTotal(),
            "CARD", // TODO: 주문에서 결제 수단 정보 가져오기
            BigDecimal.ZERO // TODO: 포인트 사용 정보 처리
        );
    }
    
    private PaymentClientDto.Response requestPaymentToPg(Long userId, OrderModel order, String callbackUrl) {
        PaymentClientDto.Request request = PaymentClientDto.Request.of(
            order.getOrderNumber().getValue(),
            order.calculateTotal(),
            order.getCardType(),
            order.getCardNumber(),
            callbackUrl
        );
        
        ApiResponse<PaymentClientDto.Response> response = paymentGatewayClient.requestPayment(
            userId.toString(), 
            request
        );
        
        if (response.meta().result() != ApiResponse.Metadata.Result.SUCCESS) {
            throw new CoreException(ErrorType.EXTERNAL_SERVER_ERROR, 
                                   "PG 결제 요청에 실패했습니다: " + response.meta().message());
        }
        
        return response.data();
    }
    
    private void publishEventBasedOnPaymentStatus(String userId, String transactionKey, PaymentClientDto.Response.Detail paymentDetail) {
        // DB에서 결제 정보를 조회하여 실제 orderId를 가져옴
        PaymentModel payment = paymentRepository.findByTransactionId(transactionKey).orElse(null);
        
        if (payment == null) {
            logger.warn("Payment not found for transactionKey={}, cannot publish event", transactionKey);
            return;
        }
        
        Long actualOrderId = payment.getOrderIdValue();
        Long actualUserId = getUserIdFromPayment(payment);
        
        if (paymentDetail.isSuccess()) {
            // 결제가 성공 상태인 경우 PaymentCompleted 이벤트 발행
            PaymentCommerceEvent.PaymentCompleted completedEvent = PaymentCommerceEvent.PaymentCompleted.of(
                transactionKey,
                actualOrderId,
                actualUserId,
                BigDecimal.valueOf(paymentDetail.amount())
            );
            eventPublisher.publishPaymentCompleted(completedEvent);
            
            logger.info("Published PaymentCompleted event for transactionKey={}, orderId={}", 
                       transactionKey, actualOrderId);
            
        } else if (paymentDetail.isFailed()) {
            // 결제가 실패 상태인 경우 PaymentFailed 이벤트 발행
            PaymentCommerceEvent.PaymentFailed failedEvent = PaymentCommerceEvent.PaymentFailed.of(
                transactionKey,
                actualOrderId,
                actualUserId,
                BigDecimal.valueOf(paymentDetail.amount()),
                paymentDetail.message() != null ? paymentDetail.message() : "Unknown error"
            );
            eventPublisher.publishPaymentFailed(failedEvent);
            
            logger.warn("Published PaymentFailed event for transactionKey={}, orderId={}, reason={}", 
                       transactionKey, actualOrderId, paymentDetail.message());
            
        } else {
            // PENDING 상태이거나 알 수 없는 상태인 경우 로그만 남김
            logger.debug("Payment status is {} for transactionKey={}, no event published", 
                        paymentDetail.status(), transactionKey);
        }
    }
    
    private Long getUserIdFromPayment(PaymentModel payment) {
        // TODO: PaymentModel에 userId 필드 추가 또는 OrderModel과의 연관관계 활용
        // 현재는 임시로 주문에서 사용자 정보를 가져옴
        OrderModel order = orderRepository.findById(payment.getOrderIdValue())
                .orElseThrow(() -> new CoreException(ErrorType.BAD_REQUEST, "주문 정보를 찾을 수 없습니다"));
        return order.getUserId().getValue();
    }
    
    /**
     * 결제 처리 결과를 담는 레코드
     */
    public record PaymentResult(
            String transactionKey,
            String status,
            Long orderId
    ) {
        public static PaymentResult of(String transactionKey, String status, Long orderId) {
            return new PaymentResult(transactionKey, status, orderId);
        }
    }
}
