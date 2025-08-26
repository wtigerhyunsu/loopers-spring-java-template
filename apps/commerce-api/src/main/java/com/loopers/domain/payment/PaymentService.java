package com.loopers.domain.payment;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private final PaymentCommerceRepository repository;


    public PaymentService(PaymentCommerceRepository repository) {
        this.repository = repository;
    }
    @Transactional
    public PaymentModel save(PaymentModel paymentModel){
        return repository.save(paymentModel);
    }
}
