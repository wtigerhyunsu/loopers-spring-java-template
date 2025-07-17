package com.loopers.domain.user;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserRepository {

    private static final Map<String, UserModel> db = new ConcurrentHashMap<>();

    public UserModel save(UserModel userModel){
        db.put(userModel.getLoginId(), userModel);
        return db.get(userModel.getLoginId());
    }
    public boolean existsByLoginId(String loginId) {
        return db.containsKey(loginId);
    }

}

