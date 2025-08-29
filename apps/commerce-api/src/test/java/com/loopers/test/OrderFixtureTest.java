package com.loopers.test;

import com.loopers.domain.order.OrderFixture;
import com.loopers.domain.order.OrderModel;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class OrderFixtureTest {

    @Test
    void orderWithId_shouldHaveId() {
        // arrange & act
        OrderModel order = OrderFixture.createOrderWithId(1L);
        
        // assert
        System.out.println("Order ID: " + order.getId());
        assertThat(order.getId()).isEqualTo(1L);
    }
}
