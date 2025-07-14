package com.loopers.interfaces.api.user;


import com.loopers.interfaces.api.ApiResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserV1Controller implements UserV1ApiSpec{

    @Override
    @PostMapping
    public ApiResponse<UserV1Dto.UserResponse> registerUser(
            @RequestBody UserV1Dto.UserRequest userV1Dto) {
        return null;
    }
}
