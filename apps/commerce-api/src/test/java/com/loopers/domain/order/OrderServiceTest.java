package com.loopers.domain.order;

import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository);
    }

    @Test
    void createOrder_ValidUserId_ReturnsOrderModel() {
        // arrange
        Long userId = 12345L;

        // act
        OrderModel result = orderService.createOrder(userId);

        // assert
        assertThat(result).isNotNull();
        assertThat(result.getUserId().getValue()).isEqualTo(userId);
        assertThat(result.getOrderNumber()).isNotNull();
        assertThat(result.getOrderNumber().getValue()).matches("^ORD-\\d{17}-[A-F0-9]{8}$");
    }

    @Test
    void completePayment_ExistingOrder_ChangesStatusToCompleted() {
        // arrange
        Long orderId = 1L;
        OrderModel order = OrderModel.register(123L);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // act
        orderService.completePayment(orderId);

        // assert
        assertThat(order.getStatus().getValue()).isEqualTo("PAYMENT_COMPLETED");
        verify(orderRepository).save(order);
    }

    @Test
    void completePayment_NonExistentOrder_ThrowsCoreException() {
        // arrange
        Long orderId = 999L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // act & assert
        assertThatThrownBy(() -> orderService.completePayment(orderId))
                .isInstanceOf(CoreException.class)
                .hasMessage("주문을 찾을 수 없습니다");
    }

    @Test
    void failPayment_ExistingOrder_ChangesStatusToFailed() {
        // arrange
        Long orderId = 1L;
        String reason = "카드 한도 초과";
        OrderModel order = OrderModel.register(123L);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // act
        orderService.failPayment(orderId, reason);

        // assert
        assertThat(order.getStatus().getValue()).isEqualTo("PAYMENT_FAILED");
        verify(orderRepository).save(order);
    }

    @Test
    void failPayment_NonExistentOrder_ThrowsCoreException() {
        // arrange
        Long orderId = 999L;
        String reason = "카드 한도 초과";
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // act & assert
        assertThatThrownBy(() -> orderService.failPayment(orderId, reason))
                .isInstanceOf(CoreException.class)
                .hasMessage("주문을 찾을 수 없습니다");
    }
}
