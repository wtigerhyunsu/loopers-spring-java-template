package com.loopers.interfaces.api.user;

public class UserV1Dto {
    public record UserRequest(
            String loginId,
            String password,
            String name,
            String email,
            String gender,
            String phoneNumber
    ){
    }
    public record UserResponse(){

    }
}
