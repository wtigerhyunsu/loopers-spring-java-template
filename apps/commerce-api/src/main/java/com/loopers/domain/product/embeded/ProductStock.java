package com.loopers.domain.product.embeded;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ProductStock {
    
    @Column(name = "stock")
    private int stock;
    
    private ProductStock(int stock) {
        this.stock = stock;
    }
    
    public ProductStock() {
    }
    
    public static ProductStock of(int stock) {
        validateStock(stock);
        return new ProductStock(stock);
    }

    public int getValue() {
        return this.stock;
    }

    private static void validateStock(int stock) {
        if (stock < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST,"재고는 0 이상이어야 합니다.");
        }
    }
    
    public boolean hasEnough(int quantity) {
        if (quantity <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST,"수량은 0보다 커야 합니다.");
        }
        return this.stock >= quantity;
    }
    
    public ProductStock decrease(int quantity) {
        if (!hasEnough(quantity)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "재고가 부족합니다.");
        }
        return new ProductStock(this.stock - quantity);
    }
    
    public ProductStock increase(int quantity) {
        if (quantity <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST,"증가할 수량은 0보다 커야 합니다.");
        }
        return new ProductStock(this.stock + quantity);
    }
}
