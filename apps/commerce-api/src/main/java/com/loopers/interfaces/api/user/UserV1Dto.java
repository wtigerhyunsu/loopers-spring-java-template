package com.loopers.interfaces.api.user;

import com.loopers.application.user.UserCommand;
import com.loopers.domain.user.UserModel;
import jakarta.validation.constraints.NotNull;

public class UserV1Dto {
    public record SignUpRequest(
            @NotNull
            String loginId,
            @NotNull
            String email,
            @NotNull
            String birth,
            @NotNull
            GenderResponse gender
    ){}
    public record SignUpResponse(

            String loginId,
            String email,
            String birth,
            GenderResponse gender
    ) {
        public static SignUpResponse from(UserCommand.UserInfo userInfo) {
            return new SignUpResponse(
                    userInfo.loginId(),
                    userInfo.email(),
                    userInfo.birth(),
                    GenderResponse.valueOf(userInfo.gender().name())
            );
        }
    }
    public record GetMeResponse(
            String loginId,
            String email,
            String birth,
            GenderResponse gender
    ) {
        public static GetMeResponse from(UserCommand.UserInfo userInfo) {
            return new GetMeResponse(
                    userInfo.loginId(),
                    userInfo.email(),
                    userInfo.birth(),
                    GenderResponse.valueOf(userInfo.gender().name())
            );
        }
    }
    public enum GenderResponse {
        M,
        F
        ;
    }
}
