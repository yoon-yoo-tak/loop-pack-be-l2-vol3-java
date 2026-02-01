package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.common.Money;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class Product extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "price", nullable = false))
    private Money price;

    @Column(name = "stock", nullable = false)
    private int stock;

    @Column(name = "like_count", nullable = false)
    private int likeCount = 0;

    @Column(name = "brand_id", nullable = false)
    private Long brandId;

    @Column(name = "brand_name", nullable = false)
    private String brandName;

    protected Product() {}

    public Product(String name, int price, int stock, Long brandId, String brandName) {
        validateName(name);
        validateStock(stock);
        validateBrandId(brandId);
        validateBrandName(brandName);

        this.name = name;
        this.price = new Money(price);
        this.stock = stock;
        this.brandId = brandId;
        this.brandName = brandName;
    }

    public void update(String name, int price, int stock) {
        validateName(name);
        validateStock(stock);

        this.name = name;
        this.price = new Money(price);
        this.stock = stock;
    }

    public void updateBrandName(String brandName) {
        validateBrandName(brandName);
        this.brandName = brandName;
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "상품 이름은 비어있을 수 없습니다.");
        }
    }

    private void validateStock(int stock) {
        if (stock < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "상품 재고는 0 이상이어야 합니다.");
        }
    }

    private void validateBrandId(Long brandId) {
        if (brandId == null || brandId <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "브랜드 ID는 양수여야 합니다.");
        }
    }

    private void validateBrandName(String brandName) {
        if (brandName == null || brandName.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "브랜드 이름은 비어있을 수 없습니다.");
        }
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price.getAmount();
    }

    public int getStock() {
        return stock;
    }

    public Long getBrandId() {
        return brandId;
    }

    public String getBrandName() {
        return brandName;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void decrementStock(int quantity) {
        if (quantity <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "수량은 0보다 커야 합니다.");
        }
        if (this.stock < quantity) {
            throw new CoreException(ErrorType.BAD_REQUEST, "재고가 부족합니다.");
        }
        this.stock -= quantity;
    }

    public void incrementLikeCount() {
        this.likeCount++;
    }

    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }
}
