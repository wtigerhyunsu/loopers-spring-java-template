package com.loopers.domain.user;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserRepository {

    private static final Map<String, UserModel> db = new ConcurrentHashMap<>();
    public UserRepository() {
        UserModel testUser = new UserModel("admin", "admin@example.com", "1990-01-01", "M");
        db.put(testUser.getLoginId(), testUser);
    }

    public UserModel save(UserModel userModel){
        db.put(userModel.getLoginId(), userModel);
        return db.get(userModel.getLoginId());
    }
    public boolean existsByLoginId(String loginId) {
        return db.containsKey(loginId);
    }

    public Optional<UserModel> findByLoginId(String loginId) {
        return Optional.ofNullable(db.get(loginId));
    }
}

