package com.loopers.domain.points;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class PointsServiceTest {
    
    @Mock
    private PointsRepository pointsRepository;
    
    @InjectMocks
    private PointsService pointsService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @DisplayName("0 이하의 정수로 포인트를 충전 시 실패한다.")
    @Test
    void chargePointsWithZeroOrNegativeValue() {
        // arrange
        BigDecimal invalidAmount = BigDecimal.valueOf(-100);
        String loginId = "testUser";

        // act
        CoreException exception = assertThrows(
            CoreException.class, 
            () -> pointsService.chargePoints(loginId, invalidAmount)
        );

        // assert
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);

        // verify: repository 호출되지 않음
        verify(pointsRepository, never()).save(any());
    }
}
