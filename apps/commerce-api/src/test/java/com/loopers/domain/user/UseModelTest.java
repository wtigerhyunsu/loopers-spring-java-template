package com.loopers.domain.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UseModelTest {

    @Nested
    @DisplayName("")
    class create {
        @DisplayName("ID, 이메일, 생년월일, 성별이 모두 주어지면, User 객체를 정상적으로 생성한다.")
        @Test
        void createsUserModel_whenAllFieldsAreProvided() {
            // arrange
            String loginId = "test123";
            String email = "test@test.net";
            String birth = "2000-08-04";
            String grender = "M"; // 성별 M
            // act
            UserModel userModel = new UserModel(loginId, email, birth, grender);

            // assert
            assertAll(
                    ()-> assertThat(userModel).isNotNull(),
                    () -> assertThat(userModel.getLoginId()).isEqualTo(loginId),
                    () -> assertThat(userModel.getEmail()).isEqualTo(email),
                    () -> assertThat(userModel.getBirth()).isEqualTo(birth),
                    () -> assertThat(userModel.getGrender()).isEqualTo(grender)
            );
        }

        @DisplayName("ID 가 영문 및 숫자 10자 이내 형식에 맞지 않으면, User 객체 생성에 실패한다.")
        @Test
        void throwsException_whenIdIsInvalid() {
            // arrange
            String loginId = "invalid_id_1234567890";
            String email = "test@test.net";
            String birth = "2000-08-04";
            String grender = "M"; // 성별 M

            // act & assert
            CoreException exception = assertThrows(CoreException.class, () -> {
                new UserModel(loginId, email, birth, grender);
            });

            // assert
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
        @DisplayName("이메일이 xx@yy.zz 형식에 맞지 않으면, User 객체 생성에 실패한다.")
        @Test
        void throwsException_whenEmailIsInvalid() {
            String loginId = "invali23";
            String email = "test@testnet";
            String birth = "2000-08-04";
            String grender = "M"; // 성별 M

            // act & assert
            CoreException exception = assertThrows(CoreException.class, () -> {
                new UserModel(loginId, email, birth, grender);
            });

            // assert
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
        @DisplayName("생년월일이 yyyy-MM-dd 형식에 맞지 않으면, User 객체 생성에 실패한다.")
        @Test
        void throwsException_whenBirthIsInvalid() {
            String loginId = "invali23";
            String email = "test@test.net";
            String birth = "20000804";
            String grender = "M"; // 성별 M

            // act & assert
            CoreException exception = assertThrows(CoreException.class, () -> {
                new UserModel(loginId, email, birth, grender);
            });

            // assert
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
        @DisplayName("생년월일이 yyyy-MM-dd 형식은 맞지만 없는 날짜거나 미래의 날짜인경우에, User 객체 생성에 실패한다.")
        @Test
        void throwsException_whenBirthIsInvalid_2() {
            String loginId = "invali23";
            String email = "test@test.net";
            String birth = "29999-08-04"; // 미래의 날짜
            String grender = "M"; // 성별 M

            // act & assert
            CoreException exception = assertThrows(CoreException.class, () -> {
                new UserModel(loginId, email, birth, grender);
            });

            // assert
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }
}
