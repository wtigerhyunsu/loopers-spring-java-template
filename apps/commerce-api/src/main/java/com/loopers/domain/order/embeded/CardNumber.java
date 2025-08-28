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
public class CardNumber {
    
    @Column(name = "card_number")
    private String value;

    protected CardNumber() {
    }

    private CardNumber(String value) {
        validateCardNumber(value);
        this.value = value;
    }

    public static CardNumber of(String cardNumber) {
        return new CardNumber(cardNumber);
    }

    private static void validateCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "카드 번호는 필수입니다.");
        }

        if (!cardNumber.matches("^\\d{4}-\\d{4}-\\d{4}-\\d{4}$")) {
            throw new CoreException(ErrorType.BAD_REQUEST, "카드 번호는 xxxx-xxxx-xxxx-xxxx 형식이어야 합니다.");
        }

        String cleanCardNumber = cardNumber.replaceAll("-", "");
        if (!isValidCardNumber(cleanCardNumber)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "유효하지 않은 카드 번호입니다.");
        }
    }

    private static boolean isValidCardNumber(String cardNumber) {
        int sum = 0;
        boolean isEvenPosition = false;
        
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));
            
            if (isEvenPosition) {
                digit *= 2;
                if (digit > 9) {
                    digit = digit / 10 + digit % 10;
                }
            }
            
            sum += digit;
            isEvenPosition = !isEvenPosition;
        }
        
        return sum % 10 == 0;
    }
}