package com.loopers.interfaces.api.payment;

import com.loopers.application.payment.PaymentCommerceService;
import com.loopers.domain.payment.PaymentCommerceEvent;
import com.loopers.interfaces.api.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentCallbackController {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentCallbackController.class);
    
    private final PaymentCommerceService paymentCommerceService;
    
    public PaymentCallbackController(PaymentCommerceService paymentCommerceService) {
        this.paymentCommerceService = paymentCommerceService;
    }
    
    @PostMapping("/callback")
    public ResponseEntity<ApiResponse<?>> handlePaymentCallback(
            @RequestBody PaymentV1Dto.CallbackRequest callbackRequest) {
        
        logger.info("Payment callback received: transactionKey={}, status={}", 
                   callbackRequest.transactionKey(), callbackRequest.status());
        
        try {
            // PG 콜백 데이터를 도메인 이벤트로 변환
            PaymentCommerceEvent.Callback callbackEvent = PaymentCommerceEvent.Callback.of(
                callbackRequest.transactionKey(),
                callbackRequest.orderId(),
                callbackRequest.status(),
                callbackRequest.reason(),
                callbackRequest.amount()
            );
            
            // 결제 콜백 처리
            paymentCommerceService.processCallback(callbackEvent);
            
            logger.info("Payment callback processed successfully: transactionKey={}", 
                       callbackRequest.transactionKey());
            
            return ResponseEntity.ok(ApiResponse.success("OK"));
            
        } catch (Exception e) {
            logger.error("Failed to process payment callback: transactionKey={}, error={}", 
                        callbackRequest.transactionKey(), e.getMessage(), e);
            
            return ResponseEntity.ok(ApiResponse.<String>fail("CALLBACK_ERROR", "콜백 처리 실패: " + e.getMessage()));
        }
    }
}
