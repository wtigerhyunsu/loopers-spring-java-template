package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.PaymentModel;
import com.loopers.domain.payment.embedded.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<PaymentModel, Long> {

    Optional<PaymentModel> findByOrderId_Value(Long orderId);
    Optional<PaymentModel> findByTransactionId_TransactionId(String transactionId);
    List<PaymentModel> findByStatus_Status(PaymentStatus.Status status);
}
