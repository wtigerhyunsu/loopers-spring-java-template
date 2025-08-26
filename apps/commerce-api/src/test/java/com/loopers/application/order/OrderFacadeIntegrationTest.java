//package com.loopers.application.order;
//
//import com.loopers.domain.brand.BrandFixture;
//import com.loopers.domain.brand.BrandModel;
//import com.loopers.domain.brand.BrandRepository;
//import com.loopers.domain.coupon.CouponModel;
//import com.loopers.domain.coupon.CouponRepository;
//import com.loopers.domain.coupon.fixture.CouponFixture;
//import com.loopers.domain.order.OrderRepository;
//import com.loopers.domain.points.PointsModel;
//import com.loopers.domain.points.PointsRepository;
//import com.loopers.domain.product.*;
//import com.loopers.domain.user.UserFixture;
//import com.loopers.domain.user.UserModel;
//import com.loopers.domain.user.UserRepository;
//import com.loopers.support.error.CoreException;
//import com.loopers.support.error.ErrorType;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.jupiter.api.Assertions.assertAll;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//
//@SpringBootTest
//@Transactional
//class OrderFacadeIntegrationTest {
//
//    @Autowired
//    private OrderFacade orderFacade;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private ProductRepository productRepository;
//
//    @Autowired
//    private BrandRepository brandRepository;
//
//    @Autowired
//    private OrderRepository orderRepository;
//
//    @Autowired
//    private CouponRepository couponRepository;
//
//    @Autowired
//    private PointsRepository pointsRepository;
//
//    private UserModel savedUser;
//    private BrandModel savedBrand;
//    private ProductModel savedProduct;
//    private CouponModel saveCoupon;
//    private PointsModel savePoint;
//
//    @BeforeEach
//    void setUp() {
//        // 모든 데이터 초기화
//        orderRepository.deleteAll();
//        productRepository.deleteAll();
//        brandRepository.deleteAll();
//        userRepository.deleteAll();
//        couponRepository.deleteAll();
//        pointsRepository.deleteAll();
//
//        // 테스트 데이터 생성
//        UserModel user = UserFixture.createUser();
//        savedUser = userRepository.save(user);
//
//        BrandModel brand = BrandFixture.createBrandModel();
//        savedBrand = brandRepository.save(brand);
//
//        ProductModel product = ProductFixture.createProductWithBrandId(savedBrand.getId());
//        savedProduct = productRepository.save(product);
//
//        CouponModel couponModel = CouponFixture.createFixedCouponWithUserId(savedUser.getId());
//        saveCoupon = couponRepository.save(couponModel);
//
//        PointsModel pointsModel = PointsModel.from(savedUser.getId(), new BigDecimal(1000000));
//        savePoint = pointsRepository.save(pointsModel);
//    }
//
//    @Nested
//    @DisplayName("[통합 테스트] 주문 생성 시")
//    class CreateOrderTest {
//
//        @DisplayName("정상적인 주문 생성 요청시 주문이 생성된다")
//        @Test
//        void createOrder_withValidRequest_shouldCreateOrder() {
//            // arrange
//
//
//            // act
//            OrderInfo.OrderItem result = orderFacade.createOrder(request);
//
//            // assert
//            assertAll(
//                () -> assertThat(result).isNotNull(),
//                () -> assertThat(result.userId()).isEqualTo(savedUser.getId()),
//                () -> assertThat(result.orderNumber()).isNotNull(),
//                () -> assertThat(result.status()).isEqualTo("PENDING_PAYMENT"),
//                () -> assertThat(result.totalPrice()).isGreaterThan(BigDecimal.ZERO)
//            );
//        }
//
//        @DisplayName("존재하지 않는 사용자로 주문 생성시 예외가 발생한다")
//        @Test
//        void createOrder_withNonExistentUser_shouldThrowException() {
//            // arrange
//            Long nonExistentUserId = 999L;
//
//
//            // act & assert
//            CoreException exception = assertThrows(CoreException.class, () -> {
//                orderFacade.createOrder(request);
//            });
//
//            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
//            assertThat(exception.getMessage()).contains("존재하지 않는 사용자 입니다");
//        }
//
//        @DisplayName("주문 아이템이 비어있을 때 예외가 발생한다")
//        @Test
//        void createOrder_withEmptyOrderItems_shouldThrowException() {
//            // arrange
//
//
//            // act & assert
//            CoreException exception = assertThrows(CoreException.class, () -> {
//                orderFacade.createOrder(request);
//            });
//
//            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
//            assertThat(exception.getMessage()).contains("주문 아이템이 비어있습니다");
//        }
//
//        @DisplayName("존재하지 않는 상품으로 주문 생성시 예외가 발생한다")
//        @Test
//        void createOrder_withNonExistentProduct_shouldThrowException() {
//            // arrange
//            Long nonExistentProductId = 999L;
//
//
//            // act & assert
//            CoreException exception = assertThrows(CoreException.class, () -> {
//                orderFacade.createOrder(request);
//            });
//
//            assertThat(exception.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
//            assertThat(exception.getMessage()).contains("존재하지 않는 상품입니다");
//        }
//
//        @DisplayName("존재하지 않는 옵션으로 주문 생성시 예외가 발생한다")
//        @Test
//        void createOrder_withNonExistentOption_shouldThrowException() {
//            // arrange
//            Long nonExistentOptionId = 999L;
//
//            // act & assert
//            CoreException exception = assertThrows(CoreException.class, () -> {
//                orderFacade.createOrder(request);
//            });
//
//            assertThat(exception.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
//            assertThat(exception.getMessage()).contains("존재하지 않는 상품 옵션입니다");
//        }
//
//        @DisplayName("수량이 0 이하일 때 예외가 발생한다")
//        @Test
//        void createOrder_withInvalidQuantity_shouldThrowException() {
//            // arrange
//
//
//            // act & assert
//            CoreException exception = assertThrows(CoreException.class, () -> {
//                orderFacade.createOrder(request);
//            });
//
//            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
//            assertThat(exception.getMessage()).contains("수량은 1 이상이어야 합니다");
//        }
//
//        @DisplayName("여러 개의 상품으로 주문 생성이 가능하다")
//        @Test
//        void createOrder_withMultipleItems_shouldCreateOrder() {
//            // arrange
//            ProductModel product2 = ProductFixture.createProductWithBrandId(savedBrand.getId());
//            ProductModel savedProduct2 = productRepository.save(product2);
//
//
//
//
//            // act
//            OrderInfo.OrderItem result = orderFacade.createOrder(request);
//
//            // assert
//            assertAll(
//                () -> assertThat(result).isNotNull(),
//                () -> assertThat(result.userId()).isEqualTo(savedUser.getId()),
//                () -> assertThat(result.totalPrice()).isGreaterThan(new BigDecimal("30000")) // 총 주문 금액 확인
//            );
//        }
//    }
//
//    @Nested
//    @DisplayName("[통합 테스트] 주문 목록 조회 시")
//    class GetOrderListTest {
//
//        @DisplayName("사용자의 주문 목록을 조회할 수 있다")
//        @Test
//        void getOrderList_withValidUser_shouldReturnOrderList() {
//            // arrange
//            // 먼저 주문을 생성
//
//            orderFacade.createOrder(createRequest);
//
//            OrderCommand.GetList request = new OrderCommand.GetList(
//                savedUser.getId(), null, 0, 10
//            );
//
//            // act
//
//            // assert
//            assertAll(
//                () -> assertThat(result).isNotNull(),
//                () -> assertThat(result.orders()).hasSize(1),
//                () -> assertThat(result.orders().get(0).userId()).isEqualTo(savedUser.getId()),
//                () -> assertThat(result.currentPage()).isEqualTo(0),
//                () -> assertThat(result.size()).isEqualTo(10)
//            );
//        }
//
//        @DisplayName("특정 상태의 주문만 필터링하여 조회할 수 있다")
//        @Test
//        void getOrderList_withStatusFilter_shouldReturnFilteredOrders() {
//            // arrange
//            // 먼저 주문을 생성
//            OrderCommand.Create createRequest = new OrderCommand.Create(
//                savedUser.getId(),
//                List.of(),
//                saveCoupon.getId(),
//                    "",""
//            );
//            orderFacade.createOrder(createRequest);
//
//            OrderCommand.GetList request = new OrderCommand.GetList(
//                savedUser.getId(), "PENDING_PAYMENT", 0, 10
//            );
//
//            // act
//            OrderInfo.ListResponse result = orderFacade.getOrderList(request);
//
//            // assert
//            assertAll(
//                () -> assertThat(result).isNotNull(),
//                () -> assertThat(result.orders()).hasSize(1),
//                () -> assertThat(result.orders().get(0).status()).isEqualTo("PENDING_PAYMENT")
//            );
//        }
//
//
//
//
//    @Nested
//    @DisplayName("[통합 테스트] 주문 상세 조회 시")
//    class GetOrderDetailTest {
//
//        @DisplayName("존재하지 않는 주문 조회시 예외가 발생한다")
//        @Test
//        void getOrderDetail_withNonExistentOrder_shouldThrowException() {
//            // arrange
//            Long nonExistentOrderId = 999L;
//            OrderCommand.GetDetail request = new OrderCommand.GetDetail(
//                nonExistentOrderId, savedUser.getId()
//            );
//
//            // act & assert
//            CoreException exception = assertThrows(CoreException.class, () -> {
//                orderFacade.getOrderDetail(request);
//            });
//
//            assertThat(exception.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
//            assertThat(exception.getMessage()).contains("존재하지 않는 주문입니다");
//        }
//    }
//
//    @Nested
//    @DisplayName("[통합 테스트] 주문 상태 변경 시")
//    class UpdateOrderStatusTest {
//
//
//
//        @DisplayName("결제 완료 처리가 정상적으로 동작한다")
//        @Test
//        void completePayment_withValidOrder_shouldCompletePayment() {
//            // arrange
//            // 먼저 주문을 생성
//            OrderCommand.Create createRequest = new OrderCommand.Create(
//                savedUser.getId(),
//                List.of(),
//                saveCoupon.getId(),
//                    "card","1241-12412-412412"
//            );
//            OrderInfo.OrderItem createdOrder = orderFacade.createOrder(createRequest);
//
//            // act & assert
//            // 현재 구현되지 않은 메서드이므로 placeholder 테스트
//            assertThat(createdOrder.status()).isEqualTo("PENDING_PAYMENT");
//        }
//    }
//}
