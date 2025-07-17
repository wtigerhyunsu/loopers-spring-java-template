package com.loopers.domain.user;

import com.loopers.interfaces.api.user.UserV1Dto;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserModel save(UserModel user) {
        try {
            return userRepository.save(user);
        }catch (Exception e){
            throw new CoreException(ErrorType.INTERNAL_ERROR);
        }
    }
    public boolean isLoginIdExists(String loginId) {
        return userRepository.existsByLoginId(loginId);
    }

    public Optional<UserModel> findByLoginId(String loginId) {
        return userRepository.findByLoginId(loginId);
    }
}
