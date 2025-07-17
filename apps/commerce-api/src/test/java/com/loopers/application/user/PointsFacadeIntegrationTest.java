package com.loopers.application.user;

import com.loopers.application.points.PointsCommand;
import com.loopers.application.points.PointsFacade;
import com.loopers.domain.points.PointsRepository;
import com.loopers.domain.points.PointsService;
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

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class PointsFacadeIntegrationTest {

    @Autowired
    private PointsFacade pointsFacade;

    @MockitoSpyBean
    private PointsService pointsService;

    @MockitoSpyBean
    private PointsRepository pointsRepository;

    @Nested
    @DisplayName("[통합 테스트]포인트 조회시")
    class get {

        @DisplayName("해당 ID 의 회원이 존재할 경우, 보유 포인트가 반환된다.")
        @Test
        void returnsPoints_whenUserExists() {
            // arrange
            String loginId = "admin";

            // act
            PointsCommand.PointInfo pointInfo = pointsFacade.getPointInfo(loginId);

            // assert
            assertAll(
                    ()-> assertThat(pointInfo.amount()).isNotNull()
            );
        }

        @DisplayName("해당 ID 의 회원이 존재하지 않을 경우, null 이 반환된다.")
        @Test
        void returnsNull_whenUserDoesNotExist() {
            // arrange
            String nonExistentLoginId = "nonExistent123";
            BigDecimal chargeAmount = BigDecimal.valueOf(100);

            // act & assert
            PointsCommand.PointInfo pointInfo =
                    pointsFacade.getPointInfo(nonExistentLoginId);

            // assert
            assertAll(
                    () -> assertThat(pointInfo).isNull()
            );
        }


    }
    @Nested
    @DisplayName("[통합 테스트]포인트 충전시")
    class post {
        @DisplayName("존재하지 않는 유저 ID 로 충전을 시도한 경우, 실패한다.")
        @Test
        void throwsException_whenChargingPointsForNonExistentUser() {
            // arrange
            String nonExistentLoginId = "nonExistent123";
            BigDecimal chargeAmount = BigDecimal.valueOf(100);

            // act & assert
            CoreException exception = assertThrows(
                    CoreException.class,
                    () -> pointsFacade.chargePoints(nonExistentLoginId, chargeAmount)
            );

            // assert
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }
}
