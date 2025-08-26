package com.loopers.domain.order;

import com.loopers.domain.order.item.OrderItemModel;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class OrderItemModelTest {

    @Nested
    @DisplayName("주문 아이템 생성 관련 테스트")
    class CreateTest {

        @DisplayName("정상적인 값으로 주문 아이템을 생성할 수 있다")
        @Test
        void create_withValidValues() {
            // arrange
            OrderModel orderModel = OrderFixture.createOrderModel();

            // act
            OrderItemModel orderItem = OrderItemFixture.createOrderItem(orderModel);

            // assert
            assertAll(
                    () -> assertThat(orderItem).isNotNull(),
                    () -> assertThat(orderItem.getProductId().getValue()).isEqualTo(OrderItemFixture.ORDER_ITEM_PRODUCT_ID),
                    () -> assertThat(orderItem.getQuantity().getValue()).isEqualTo(OrderItemFixture.ORDER_ITEM_QUANTITY),
                    () -> assertThat(orderItem.getOrderItemPrice().getValue()).isEqualTo(OrderItemFixture.ORDER_ITEM_PRICE_PER_UNIT)
            );
        }

        @DisplayName("상품 ID가 null이면 생성에 실패한다")
        @Test
        void create_whenProductIdNull() {
            // arrange
            OrderModel orderModel = OrderFixture.createOrderModel();
            Long productId = null;

            // act & assert
            CoreException exception = assertThrows(CoreException.class, () -> {
                OrderItemFixture.createWithProductId(orderModel, productId);
            });

            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(exception.getMessage()).contains("id cannot be null");
        }

        // 옵션 ID 관련 테스트는 새로운 도메인 모델에서 제거됨

        @DisplayName("음수 수량으로 생성에 실패한다")
        @Test
        void create_withNegativeQuantity() {
            // arrange
            OrderModel orderModel = OrderFixture.createOrderModel();
            int quantity = -1;

            // act & assert
            CoreException exception = assertThrows(CoreException.class, () -> {
                OrderItemFixture.createWithQuantity(orderModel, quantity);
            });

            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(exception.getMessage()).contains("수량은 1 이상이어야 합니다");
        }

        @DisplayName("수량이 999를 초과하면 생성에 실패한다")
        @Test
        void create_withQuantityOverLimit() {
            // arrange
            OrderModel orderModel = OrderFixture.createOrderModel();
            int quantity = 1000;

            // act & assert
            CoreException exception = assertThrows(CoreException.class, () -> {
                OrderItemFixture.createWithQuantity(orderModel, quantity);
            });

            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(exception.getMessage()).contains("수량은 999개를 초과할 수 없습니다");
        }

        @DisplayName("음수 단가로 생성에 실패한다")
        @Test
        void create_withNegativePrice() {
            // arrange
            OrderModel orderModel = OrderFixture.createOrderModel();
            BigDecimal pricePerUnit = new BigDecimal("-1000");

            // act & assert
            CoreException exception = assertThrows(CoreException.class, () -> {
                OrderItemFixture.createWithPrice(orderModel, pricePerUnit);
            });

            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(exception.getMessage()).contains("단가는 0 이상이어야 합니다");
        }

        // 상품명 관련 테스트는 새로운 도메인 모델에서 제거됨

        // 빈 상품명 관련 테스트는 새로운 도메인 모델에서 제거됨
    }

    @Nested
    @DisplayName("소계 계산 관련 테스트")
    class SubtotalTest {

        @DisplayName("소계를 올바르게 계산할 수 있다")
        @Test
        void subtotal_calculate() {
            // arrange
            OrderModel orderModel = OrderFixture.createOrderModel();
            int quantity = 3;
            BigDecimal pricePerUnit = new BigDecimal("15000");
            OrderItemModel orderItem = OrderItemFixture.createOrderItem(
                    orderModel, quantity, pricePerUnit
            );
            BigDecimal expectedSubtotal = pricePerUnit.multiply(new BigDecimal(quantity));

            // act
            BigDecimal subtotal = orderItem.subtotal();

            // assert
            assertThat(subtotal).isEqualByComparingTo(expectedSubtotal);
        }

        @DisplayName("수량이 0일 때 소계는 0이다")
        @Test
        void subtotal_withZeroQuantity() {
            // arrange
            OrderModel orderModel = OrderFixture.createOrderModel();
            int quantity = 0;
            BigDecimal pricePerUnit = new BigDecimal("15000");
            OrderItemModel orderItem = OrderItemFixture.createOrderItem(
                    orderModel, 1L, 1L, quantity, pricePerUnit
            );

            // act
            BigDecimal subtotal = orderItem.subtotal();

            // assert
            assertThat(subtotal).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @DisplayName("단가가 0일 때 소계는 0이다")
        @Test
        void subtotal_withZeroPrice() {
            // arrange
            OrderModel orderModel = OrderFixture.createOrderModel();
            int quantity = 5;
            BigDecimal pricePerUnit = BigDecimal.ZERO;
            OrderItemModel orderItem = OrderItemFixture.createOrderItem(
                    orderModel, 1L, 1L, quantity, pricePerUnit
            );

            // act
            BigDecimal subtotal = orderItem.subtotal();

            // assert
            assertThat(subtotal).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @DisplayName("기본 Fixture로 생성된 아이템의 소계를 계산할 수 있다")
        @Test
        void subtotal_withFixture() {
            // arrange
            OrderModel orderModel = OrderFixture.createOrderModel();
            OrderItemModel orderItem = OrderItemFixture.createOrderItem(orderModel);
            BigDecimal expectedSubtotal =
                    OrderItemFixture.ORDER_ITEM_PRICE_PER_UNIT.multiply(
                            new BigDecimal(OrderItemFixture.ORDER_ITEM_QUANTITY));

            // act
            BigDecimal subtotal = orderItem.subtotal();

            // assert
            assertThat(subtotal).isEqualByComparingTo(expectedSubtotal);
        }
    }

    @Nested
    @DisplayName("Fixture를 사용한 테스트")
    class FixtureTest {

        @DisplayName("기본 Fixture로 주문 아이템을 생성할 수 있다")
        @Test
        void createWithDefaultFixture() {
            // arrange
            OrderModel orderModel = OrderFixture.createOrderModel();

            // act
            OrderItemModel orderItem = OrderItemFixture.createOrderItem(orderModel);

            // assert
            assertAll(
                    () -> assertThat(orderItem).isNotNull(),
                    () -> assertThat(orderItem.getProductId().getValue()).isEqualTo(OrderItemFixture.ORDER_ITEM_PRODUCT_ID),
                    () -> assertThat(orderItem.getQuantity().getValue()).isEqualTo(OrderItemFixture.ORDER_ITEM_QUANTITY),
                    () -> assertThat(orderItem.getOrderItemPrice().getValue()).isEqualTo(OrderItemFixture.ORDER_ITEM_PRICE_PER_UNIT)
            );
        }

        @DisplayName("특정 가격으로 아이템 Fixture를 생성할 수 있다")
        @Test
        void createWithSpecificPrice() {
            // arrange
            OrderModel orderModel = OrderFixture.createOrderModel();
            BigDecimal price = new BigDecimal("25000");

            // act
            OrderItemModel orderItem = OrderItemFixture.createWithPrice(orderModel, price);

            // assert
            assertThat(orderItem.getOrderItemPrice().getValue()).isEqualByComparingTo(price);
        }

        @DisplayName("특정 수량으로 아이템 Fixture를 생성할 수 있다")
        @Test
        void createWithSpecificQuantity() {
            // arrange
            OrderModel orderModel = OrderFixture.createOrderModel();
            int quantity = 5;

            // act
            OrderItemModel orderItem = OrderItemFixture.createWithQuantity(orderModel, quantity);

            // assert
            assertThat(orderItem.getQuantity().getValue()).isEqualByComparingTo(quantity);
        }

        @DisplayName("특정 상품 ID로 아이템 Fixture를 생성할 수 있다")
        @Test
        void createWithSpecificProductId() {
            // arrange
            OrderModel orderModel = OrderFixture.createOrderModel();
            Long productId = 999L;

            // act
            OrderItemModel orderItem = OrderItemFixture.createWithProductId(orderModel, productId);

            // assert
            assertThat(orderItem.getProductId().getValue()).isEqualTo(productId);
        }
    }
}
