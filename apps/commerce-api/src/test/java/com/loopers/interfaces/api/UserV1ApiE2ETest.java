package com.loopers.interfaces.api;

import com.loopers.domain.user.UserModel;
import com.loopers.interfaces.api.user.UserV1Dto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserV1ApiE2ETest {
    private final TestRestTemplate testRestTemplate;
    @Autowired
    public UserV1ApiE2ETest(TestRestTemplate testRestTemplate) {
        this.testRestTemplate = testRestTemplate;
    }

    @Nested
    @DisplayName("POST /api/v1/users")
    class post{
        static String ENDPOINT = "/api/v1/users";
        @DisplayName("회원 가입이 성공할 경우, 생성된 유저 정보를 응답으로 반환한다.")
        @Test
        void returnsCreatedUserInfo_whenRegistrationIsSuccessful() {

            UserV1Dto.SignUpRequest request  = new UserV1Dto.SignUpRequest(
                    "testUser",
                    "test@naver.com",
                    "2000-08-04",
                    UserV1Dto.GenderResponse.M // 성별 M
            );
             ParameterizedTypeReference<ApiResponse<UserV1Dto.SignUpResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.SignUpResponse>> response =
                    testRestTemplate.exchange(ENDPOINT, HttpMethod.POST, new HttpEntity<>(request),responseType);
            System.out.println(response.getBody());
            assertAll(
                    () -> assertThat(response.getBody()).isNotNull(),
                    () -> assertThat(response.getBody().meta().result()).isEqualTo(ApiResponse.Metadata.Result.SUCCESS),
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK)

            );
        }
        @DisplayName("회원 가입 시에 성별이 없을 경우, `400 Bad Request` 응답을 반환한다.")
        @Test
        void returnsBadRequest_whenRegistrationIsNotSuccessful() {
            //arrange
            UserV1Dto.SignUpRequest request  = new UserV1Dto.SignUpRequest(
                    "testUser",
                    "test@test.net",
                    "2000-08-04",
                    null // 성별이 없을 경우
            );
            //act
            ParameterizedTypeReference<ApiResponse<UserV1Dto.SignUpResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.SignUpResponse>> response =
                    testRestTemplate.exchange(ENDPOINT, HttpMethod.POST, new HttpEntity<>(request),responseType);
            //assert
            System.out.println(response.getStatusCode());
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
                    );

        }
    }
    @Nested
    @DisplayName("GET /api/v1/users/me")
    class get {
        static String ENDPOINT = "/api/v1/users/me";

        @DisplayName("사용자 정보를 조회할 때, 올바른 사용자 ID를 제공하면 해당 사용자 정보를 반환한다.")
        @Test
        void returnsUserInfo_whenValidUserIdIsProvided() {
            // arrange
            String userId = "admin";

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-USER-ID", userId );

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            // act
            ParameterizedTypeReference<ApiResponse<UserV1Dto.SignUpResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.SignUpResponse>> response =
                    testRestTemplate.exchange(ENDPOINT, HttpMethod.GET, entity, responseType);

            // assert
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(response.getBody().data().loginId()).isEqualTo(userId)
            );
        }

        @DisplayName("사용자 정보를 조회할 때, 사용자 ID가 제공되지 않으면 `400 Bad Request` 응답을 반환한다.")
        @Test
        void returnsBadRequest_whenUserIdIsNotProvided() {
            // arrange
            String userId = "";
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-USER-ID", userId );

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            // act
            ParameterizedTypeReference<ApiResponse<UserV1Dto.SignUpResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.SignUpResponse>> response =
                    testRestTemplate.exchange(ENDPOINT, HttpMethod.GET, entity, responseType);
            // assert
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
            );
        }

        @DisplayName("사용자 정보를 조회할 때, X-USER-ID 헤더가 없으면 `400 Bad Request` 응답을 반환한다.")
        @Test
        void returnsBadRequest_whenUserIdHeaderIsMissing() {
            // arrange
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            // act
            ParameterizedTypeReference<ApiResponse<UserV1Dto.SignUpResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.SignUpResponse>> response =
                    testRestTemplate.exchange(ENDPOINT, HttpMethod.GET, entity, responseType);

            // assert
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
            );
        }
    }


}
