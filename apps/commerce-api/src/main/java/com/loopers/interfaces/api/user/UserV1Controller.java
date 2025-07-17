package com.loopers.interfaces.api.user;


import com.loopers.application.user.UserCommand;
import com.loopers.application.user.UserFacade;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserV1Controller implements UserV1ApiSpec{
    private final UserFacade userFacade;

    public UserV1Controller(UserFacade userFacade) {
        this.userFacade = userFacade;
    }

    @Override
    @PostMapping
    public ApiResponse<UserV1Dto.SignUpResponse> signUp(
           @Valid @RequestBody UserV1Dto.SignUpRequest userV1Dto) {
        UserV1Dto.SignUpResponse response = UserV1Dto.SignUpResponse.from(userFacade.signUpUser(userV1Dto));
        return ApiResponse.success(response);
    }
    @Override
    @GetMapping(value = "/me")
    public ApiResponse<UserV1Dto.GetMeResponse> getMe(
            @NotNull @NotBlank @RequestHeader("X-USER-ID") String userId) {
        String loginId = "TestUserId";
        if(!loginId.equals(userId)){
            throw new CoreException(ErrorType.BAD_REQUEST,"아이디가 일치하지않아요");// 예시로 고정된 값 사용
        }
        return ApiResponse.success(
                new UserV1Dto.GetMeResponse(
                        userId
                )
        );

    }

}

