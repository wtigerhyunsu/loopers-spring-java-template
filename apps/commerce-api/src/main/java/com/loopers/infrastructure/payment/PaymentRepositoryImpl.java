package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.PaymentModel;
import com.loopers.domain.payment.PaymentCommerceRepository;
import com.loopers.domain.payment.embedded.PaymentStatus;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PaymentRepositoryImpl implements PaymentCommerceRepository {
    private final PaymentJpaRepository paymentJpaRepository;

    public PaymentRepositoryImpl(PaymentJpaRepository paymentJpaRepository) {
        this.paymentJpaRepository = paymentJpaRepository;
    }

    @Override
    public PaymentModel save(PaymentModel paymentModel) {
        return paymentJpaRepository.save(paymentModel);
    }

    @Override
    public Optional<PaymentModel> findById(Long id) {
        return paymentJpaRepository.findById(id);
    }
    
    @Override
    public Optional<PaymentModel> findByOrderId(Long orderId) {
        // TODO: PaymentJpaRepository에 해당 메서드 추가 필요
        return paymentJpaRepository.findByOrderId_Value(orderId);
    }
    
    @Override
    public Optional<PaymentModel> findByTransactionId(String transactionId) {
        // TODO: PaymentJpaRepository에 해당 메서드 추가 필요  
        return paymentJpaRepository.findByTransactionId_TransactionId(transactionId);
    }
    
    @Override
    public List<PaymentModel> findByUserId(Long userId) {
        // TODO: PaymentModel에 userId 필드 추가 후 구현
        return List.of(); // 임시 구현
    }
    
    @Override
    public List<PaymentModel> findByStatus(String status) {
        // PaymentStatus.Status enum 사용
        try {
            PaymentStatus.Status statusEnum = PaymentStatus.Status.valueOf(status.toUpperCase());
            return paymentJpaRepository.findByStatus_Status(statusEnum);
        } catch (IllegalArgumentException e) {
            return List.of(); // 잘못된 상태값인 경우 빈 리스트 반환
        }
    }

    @Override
    public void deleteById(Long id) {
        paymentJpaRepository.deleteById(id);
    }
    
    @Override
    public boolean existsById(Long id) {
        return paymentJpaRepository.existsById(id);
    }
}
