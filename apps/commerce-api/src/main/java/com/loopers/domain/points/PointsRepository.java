package com.loopers.domain.points;

import com.loopers.domain.user.UserModel;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PointsRepository {

    private static final Map<String, PointsModel> db = new ConcurrentHashMap<>();

    public PointsRepository() {
        PointsModel initialPoints = new PointsModel("admin", BigDecimal.ZERO);
        db.put(initialPoints.getUserId(), initialPoints);
    }

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

