package com.loopers.domain.order.embeded;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Embeddable
@Getter
@EqualsAndHashCode
public class CardType {
    
    @Column(name = "card_type")
    private String value;

    protected CardType() {
    }

    private CardType(String value) {
        validateCardType(value);
        this.value = value;
    }

    public static CardType of(String cardType) {
        return new CardType(cardType);
    }

    private static void validateCardType(String cardType) {
        if (cardType == null || cardType.trim().isEmpty()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "카드 타입은 필수입니다.");
        }
        
        if (!cardType.matches("^(SAMSUNG|KB|HYUNDAI)$")) {
            throw new CoreException(ErrorType.BAD_REQUEST, "지원하지 않는 카드 타입입니다. 허용된 타입: SAMSUNG, KB, HYUNDAI");
        }
    }
}