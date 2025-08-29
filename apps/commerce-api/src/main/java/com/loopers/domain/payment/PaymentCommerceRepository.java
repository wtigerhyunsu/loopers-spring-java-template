package com.loopers.domain.payment;

import java.util.List;
import java.util.Optional;

/**
 * 결제 도메인 리포지토리 인터페이스
 * 
 * DDD 아키텍처에서 도메인 계층의 영속성 추상화를 담당합니다.
 */
public interface PaymentCommerceRepository {
    
    /**
     * 결제 정보를 저장합니다.
     * 
     * @param paymentModel 저장할 결제 모델
     * @return 저장된 결제 모델
     */
    PaymentModel save(PaymentModel paymentModel);
    
    /**
     * ID로 결제 정보를 조회합니다.
     * 
     * @param id 결제 ID
     * @return 결제 모델 Optional
     */
    Optional<PaymentModel> findById(Long id);
    
    /**
     * 주문 ID로 결제 정보를 조회합니다.
     * 
     * @param orderId 주문 ID
     * @return 결제 모델 Optional
     */
    Optional<PaymentModel> findByOrderId(Long orderId);
    
    /**
     * 트랜잭션 ID로 결제 정보를 조회합니다.
     * 
     * @param transactionId PG 트랜잭션 ID
     * @return 결제 모델 Optional
     */
    Optional<PaymentModel> findByTransactionId(String transactionId);
    
    /**
     * 사용자의 모든 결제 내역을 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 결제 모델 리스트
     */
    List<PaymentModel> findByUserId(Long userId);
    
    /**
     * 특정 상태의 결제 내역을 조회합니다.
     * 
     * @param status 결제 상태
     * @return 결제 모델 리스트
     */
    List<PaymentModel> findByStatus(String status);
    
    /**
     * ID로 결제 정보를 삭제합니다.
     * 
     * @param id 결제 ID
     */
    void deleteById(Long id);
    
    /**
     * 결제 정보의 존재 여부를 확인합니다.
     * 
     * @param id 결제 ID
     * @return 존재 여부
     */
    boolean existsById(Long id);
}
