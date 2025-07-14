package com.loopers.interfaces.api.user;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "User V1 API", description = "Loopers 사용자 API 입니다.")
public interface UserV1ApiSpec {
    @Operation(
        summary = "회원 가입",
        description = "사용자를 등록합니다. 성공 시 생성된 사용자 정보를 반환합니다."
    )
    ApiResponse<UserV1Dto.UserResponse> registerUser(
            @Schema(name = "예시 ID", description = "조회할 예시의 ID")
            UserV1Dto.UserRequest userV1Dto
    );
}
