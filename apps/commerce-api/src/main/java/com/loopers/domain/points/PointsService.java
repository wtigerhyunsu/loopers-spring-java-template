package com.loopers.domain.points;

import com.loopers.domain.user.UserModel;
import com.loopers.domain.user.UserRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class PointsService {
    private final PointsRepository pointsRepository;

    public PointsService(PointsRepository pointsRepository) {
        this.pointsRepository = pointsRepository;
    }

    public PointsModel save(PointsModel user) {
        try {
            return pointsRepository.save(user);
        }catch (Exception e){
            throw new CoreException(ErrorType.INTERNAL_ERROR);
        }
    }

    public PointsModel getByLoginId(String loginId) {
        if(!pointsRepository.existsByLoginId(loginId)){
            pointsRepository.save(
                    new PointsModel(loginId, BigDecimal.ZERO)
            );
        }
        return pointsRepository.findByLoginId(loginId).orElse(null);
    }
}
