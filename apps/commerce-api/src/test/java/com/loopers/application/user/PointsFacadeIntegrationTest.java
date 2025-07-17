package com.loopers.application.user;

import com.loopers.domain.user.UserModel;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.UserService;
import com.loopers.interfaces.api.user.UserV1Dto;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class PointsFacadeIntegrationTest {

    @Autowired
    private UserFacade userFacade;

    @MockitoSpyBean
    private UserService userService;

    @MockitoSpyBean
    private UserRepository userRepository;

    @Nested
    @DisplayName("[통합 테스트]회원 가입 시")
    class SignUp {

        @DisplayName("회원 가입시 User 저장이 수행된다.")
        @Test
        void saveUser_whenSignUpIsSuccessful() {
            // arrange
            UserV1Dto.SignUpRequest request = new UserV1Dto.SignUpRequest(
                    "newUser123",
                    "new@example.com",
                    "1995-05-15",
                    UserV1Dto.GenderResponse.F
            );

            // act
            UserCommand.UserInfo result = userFacade.signUpUser(request);

            // assert
            assertThat(result).isNotNull();
            assertThat(result.loginId()).isEqualTo("newUser123");
            assertThat(result.email()).isEqualTo("new@example.com");
            assertThat(result.birth()).isEqualTo("1995-05-15");
            assertThat(result.gender()).isEqualTo(UserCommand.Gender.F);

            verify(userService).save(any());
            verify(userRepository).save(any());
        }

        @DisplayName("이미 가입된 ID 로 회원가입 시도 시, 실패한다.")
        @Test
        void throwsException_whenLoginIdAlreadyExists() {
            // arrange
            UserV1Dto.SignUpRequest request = new UserV1Dto.SignUpRequest(
                    "admin", // 이미 존재하는 ID
                    "admin2@example.com",
                    "1990-01-01",
                    UserV1Dto.GenderResponse.M
            );

            // act
            CoreException exception = assertThrows(
                    CoreException.class,
                    () -> userFacade.signUpUser(request)
            );
            // assert
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(exception.getMessage()).contains("이미 존재하는 ID입니다");
        }
    }
    @DisplayName("해당 ID 의 회원이 존재할 경우, 회원 정보가 반환된다.")
    @Test
    void returnsUserInfo_whenUserExists() {
        // arrange
        String loginId = "test123";
        String email = "test@test.net";
        String birth = "2000-08-04";
        String grender = "M"; // 성별 M
        UserModel userModel = new UserModel(loginId, email, birth, grender);

        userRepository.save(userModel);

        // act
        UserCommand.UserInfo userInfo = userFacade.getUserById(userModel.getLoginId());

        // assert
        AssertionsForClassTypes.assertThat(userInfo).isNotNull();
        AssertionsForClassTypes.assertThat(userInfo.loginId()).isEqualTo(loginId);
        AssertionsForClassTypes.assertThat(userInfo.email()).isEqualTo(email);
        AssertionsForClassTypes.assertThat(userInfo.birth()).isEqualTo(birth);
        AssertionsForClassTypes.assertThat(userInfo.gender()).isEqualTo(UserCommand.Gender.M);
    }

    @DisplayName("해당 ID 의 회원이 존재하지 않을 경우,  null 이 반환된다.")
    @Test
    void throwsException_whenUserDoesNotExist() {
        // arrange
        String nonExistentLoginId = "nonExistent123";

        // act
        UserCommand.UserInfo info = userFacade.getUserById(nonExistentLoginId);

        // assert
        AssertionsForClassTypes.assertThat(info).isNull();
    }
}
