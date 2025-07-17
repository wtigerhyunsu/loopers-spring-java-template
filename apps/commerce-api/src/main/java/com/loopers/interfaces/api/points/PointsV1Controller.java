package com.loopers.interfaces.api.points;

import com.loopers.application.points.PointsFacade;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/points")
public class PointsV1Controller implements PointsV1ApiSpec {
    private final PointsFacade pointsFacade;

    public PointsV1Controller(PointsFacade pointsFacade) {
        this.pointsFacade = pointsFacade;
    }

    @Override
    @GetMapping
    public ApiResponse<PointsV1Dto.PointsResponse> getPoints(
            @RequestHeader("X-USER-ID") String userId) {
        if(userId == null || userId.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용자 ID가 필요합니다.");
        }
        return ApiResponse.success(
                PointsV1Dto.PointsResponse
                        .from(pointsFacade.getPointInfo(userId))
        );

    }
    @Override
    @PostMapping("/charge")
    public ApiResponse<PointsV1Dto.PointsResponse> chargePoints(
            @RequestHeader("X-USER-ID") String userId,
            @RequestBody PointsV1Dto.PointsRequest pointsV1Request) {
        BigDecimal amount = pointsV1Request.amount();
        if(userId == null || userId.isBlank() || !userId.equals("admin")) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용자 ID가 필요합니다.");
        }

        if(amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "충전 금액은 0보다 커야 합니다.");
        }
        return ApiResponse.success(
                PointsV1Dto.PointsResponse.of(amount)
        );
    }

}
