package com.loopers.domain.user;

import com.loopers.application.user.UserCommand;
import com.loopers.application.user.UserFacade;
import com.loopers.interfaces.api.user.UserV1Dto;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

@SpringBootTest
public class UserServiceTest {
    @Autowired
    private UserFacade userFacade;

    @Autowired
    private UserRepository userRepository;

    @DisplayName(" 회원 가입시,")
    @Nested
    class get {
        @DisplayName("이미 가입된 Id가 있는 경우 실패 한다.")
        @Test
        void throwsException_whenIdAlreadyExists() {
            // arrange
            String loginId = "test123";
            String email = "test@test.net";
            String birth = "2000-08-04";
            String grender = "M"; // 성별 M
            UserModel userModel = new UserModel(loginId, email, birth, grender);

            UserV1Dto.SignUpRequest request = new UserV1Dto.SignUpRequest(
                userModel.getLoginId(),
                userModel.getEmail(),
                userModel.getBirth(),
                UserV1Dto.GenderResponse.valueOf(userModel.getGrender())
            );

            userRepository.save(userModel);

            // act
            CoreException exception = assertThrows(
                CoreException.class, () -> userFacade.signUpUser(request));

            //assert
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
        


    }



}
