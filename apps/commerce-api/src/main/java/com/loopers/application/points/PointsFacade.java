package com.loopers.application.points;

import com.loopers.application.user.UserCommand;
import com.loopers.domain.points.PointsModel;
import com.loopers.domain.points.PointsService;
import com.loopers.domain.user.UserModel;
import com.loopers.domain.user.UserService;
import com.loopers.interfaces.api.user.UserV1Dto;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.springframework.stereotype.Component;

@Component
public class PointsFacade {
    private final PointsService pointsService;
    private final UserService userService;

    public PointsFacade(PointsService pointsService, UserService userService) {
        this.pointsService = pointsService;
        this.userService = userService;
    }

    public PointsCommand.PointInfo getPointInfo(String loginId) {
        if(!userService.isLoginIdExists(loginId)){
            throw new CoreException(ErrorType.BAD_REQUEST, "사용자를 찾을 수 없습니다.");
        }
        PointsModel pointsModel = pointsService.getByLoginId(loginId);
        if(pointsModel == null) {
            throw new CoreException(ErrorType.NOT_FOUND);
        }
        return PointsCommand.PointInfo.from(pointsModel);

    }
}
