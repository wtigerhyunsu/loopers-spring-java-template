package com.loopers.domain.points;

import com.loopers.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter@Setter
public class PointsModel extends BaseEntity {

    private String userId;
    private BigDecimal amount;


    public PointsModel(String loginId, BigDecimal zero) {
        this.userId = loginId;
        this.amount = zero;
    }
}
