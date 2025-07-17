package com.loopers.domain.points;

import com.loopers.domain.user.UserModel;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PointsRepository {

    private static final Map<String, PointsModel> db = new ConcurrentHashMap<>();

    public PointsModel save(PointsModel pointsModel) {
        db.put(pointsModel.getUserId(), pointsModel);
        return db.get(pointsModel.getUserId());
    }
    public boolean existsByLoginId(String loginId) {
        return db.containsKey(loginId);
    }

    public Optional<PointsModel> findByLoginId(String loginId) {
        return Optional.ofNullable(db.get(loginId));
    }
}

