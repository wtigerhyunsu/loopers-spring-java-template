package com.loopers.application.points;

import com.loopers.domain.points.PointsModel;
import com.loopers.domain.user.UserModel;
import com.loopers.interfaces.api.points.PointsV1Dto;
import com.loopers.interfaces.api.user.UserV1Dto;

import java.math.BigDecimal;


public class PointsCommand {
    public record PointInfo(
            String loginId,
            BigDecimal amount){
        public static PointInfo from(PointsModel pointsModel) {
            return new PointInfo(
                    pointsModel.getUserId(),
                    pointsModel.getAmount()
            );
        }
    }
}
