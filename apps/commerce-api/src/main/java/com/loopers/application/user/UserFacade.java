package com.loopers.application.user;

import com.loopers.domain.user.UserModel;
import com.loopers.domain.user.UserService;
import com.loopers.interfaces.api.user.UserV1Dto;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.springframework.stereotype.Component;

@Component
public class UserFacade {
    private final UserService userService;

    public UserFacade(UserService userService) {
        this.userService = userService;
    }

    public UserCommand.UserInfo signUpUser(UserV1Dto.SignUpRequest request) {

        UserCommand.SignUp command = UserCommand.SignUp.from(request);

        if (userService.isLoginIdExists(command.loginId())) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이미 존재하는 ID입니다.");
        }

        UserModel user = new UserModel(
                command.loginId(),
                command.email(),
                command.birth(),
                command.gender().toString()
        );
        UserModel saved = userService.save(user);
        return UserCommand.UserInfo.from(saved);
    }
}
