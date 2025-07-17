package com.loopers.application.user;

import com.loopers.domain.user.UserModel;
import com.loopers.interfaces.api.user.UserV1Dto;

public class UserCommand {
    public record SignUp(
            String loginId,
            String email,
            String birth,
            Gender gender){
        public static SignUp from(UserV1Dto.SignUpRequest request) {
            return new SignUp(
                    request.loginId(),
                    request.email(),
                    request.birth(),
                    Gender.valueOf(request.gender().name())
            );
        }
    }
    public record UserInfo(
            String loginId,
            String email,
            String birth,
            Gender gender){
        public static UserInfo from(UserModel user) {
            return new UserInfo(
                    user.getLoginId(),
                    user.getEmail(),
                    user.getBirth(),
                    Gender.valueOf(user.getGrender())
            );
        }
    }


    public enum Gender {
        M,
        F;
        public static Gender from(UserV1Dto.GenderResponse gender) {
            return Gender.valueOf(gender.name());
        }
    }
}
