package com.loopers.domain.payment;

public class PaymentCommerceEvent {
    public record Request(
            Long orderId
    ){
    }
    public record Completed(){

    }
    public record Failed(){

    }
    public record Cancelled(){

    }
}

