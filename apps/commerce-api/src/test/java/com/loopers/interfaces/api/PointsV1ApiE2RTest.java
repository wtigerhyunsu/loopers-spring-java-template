package com.loopers.interfaces.api;

import com.loopers.interfaces.api.points.PointsV1ApiSpec;
import com.loopers.interfaces.api.points.PointsV1Dto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.UserModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PointsV1ApiE2RTest {
    private final TestRestTemplate testRestTemplate;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    public PointsV1ApiE2RTest(TestRestTemplate testRestTemplate) {
        this.testRestTemplate = testRestTemplate;
    }
    
    @BeforeEach
    void setUp() {
        // 테스트 전에 admin 사용자가 있는지 확인하고 없으면 생성
        if (!userRepository.existsByLoginId("admin")) {
            UserModel adminUser = new UserModel("admin", "admin@example.com", "1990-01-01", "M");
            userRepository.save(adminUser);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/points")
    class get {
        static String ENDPOINT = "/api/v1/points";

        @DisplayName("포인트 조회에 성공할 경우, 보유 포인트를 응답으로 반환한다.")
        @Test
        void returnsPoints_whenPointRetrievalIsSuccessful() {
            // arrange
            String userId = "admin";

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-USER-ID", userId );

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            // act
            ParameterizedTypeReference<ApiResponse<PointsV1Dto.PointsResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<PointsV1Dto.PointsResponse>> response =
                    testRestTemplate.exchange(ENDPOINT, HttpMethod.GET, entity, responseType);

            // assert
            assertAll(
                    ()-> assertThat(response.getStatusCode().is2xxSuccessful()).isTrue(),
                    ()-> assertThat(response.getBody()).isNotNull(),
                    ()-> assertThat(response.getBody().data().amount()).isEqualTo(new BigDecimal("1000.00"))
            );
        }
        @DisplayName("X-USER-ID 헤더가 없을 경우, 400 Bad Request 응답을 반환한다.")
        @Test
        void  returnsBadRequest_whenPointRetrievalIsNotSuccessful() {
            // arrange
            HttpHeaders headers = new HttpHeaders();

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            // act
            ParameterizedTypeReference<ApiResponse<PointsV1Dto.PointsResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<PointsV1Dto.PointsResponse>> response =
                    testRestTemplate.exchange(ENDPOINT, HttpMethod.GET, entity, responseType);

            // assert
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
            );
        }
    }
    @Nested
    @DisplayName("POST /api/v1/points")
    class post{
        static String ENDPOINT = "/api/v1/points/charge";

        @DisplayName("존재하는 유저가 1000원을 충전할 경우, 충전된 보유 총량을 응답으로 반환한다.")
        @Test
        void returnsPoints_whenPointRetrievalIsSuccessful() {
            // arrange
            String userId = "admin";
            String points = "1000.00";

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-USER-ID", userId );
            PointsV1Dto.PointsRequest pointsV1Request = new PointsV1Dto.PointsRequest(new BigDecimal(points));

            HttpEntity<PointsV1Dto.PointsRequest> entity = new HttpEntity<>(pointsV1Request, headers);

            // act
            ParameterizedTypeReference<ApiResponse<PointsV1Dto.PointsResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<PointsV1Dto.PointsResponse>> response =
                    testRestTemplate.exchange(ENDPOINT, HttpMethod.POST, entity, responseType);
            System.out.println(response.getBody());
            // assert
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                    () -> assertThat(response.getBody()).isNotNull(),
                    () -> assertThat(response.getBody().data().amount()).isEqualTo(points)
            );
        }
        @DisplayName("존재하지 않는 유저로 요청할 경우, 404 Not Found 응답을 반환한다.")
        @Test
        void returnsNotFound_whenUserDoesNotExist() {
            // arrange
            String userId = "Testadmin";

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-USER-ID", userId );
            PointsV1Dto.PointsRequest pointsV1Request = new PointsV1Dto.PointsRequest(new BigDecimal("1000.00"));

            HttpEntity<PointsV1Dto.PointsRequest> entity = new HttpEntity<>(pointsV1Request, headers);

            // act
            ParameterizedTypeReference<ApiResponse<PointsV1Dto.PointsResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<PointsV1Dto.PointsResponse>> response =
                    testRestTemplate.exchange(ENDPOINT, HttpMethod.POST, entity, responseType);
            // assert
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
            );
        }
    }
}
