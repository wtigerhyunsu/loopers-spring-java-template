package com.loopers.interfaces.api.points;

import com.loopers.application.points.PointsCommand;

import java.math.BigDecimal;

public class PointsV1Dto {
    public record PointsRequest(
        BigDecimal amount
    ) {
        public static PointsRequest of(BigDecimal amount) {
            return new PointsRequest(amount);
        }
    }
    public record PointsResponse(
        BigDecimal amount
    ) {
        public static PointsResponse of(BigDecimal points) {
            return new PointsResponse(points);
        }

        public static PointsResponse from(PointsCommand.PointInfo pointInfo) {
            return new PointsResponse(pointInfo.amount());
        }
    }
}
