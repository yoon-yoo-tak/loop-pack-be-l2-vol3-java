package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.common.Money;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_items")
public class OrderItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "product_price", nullable = false))
    private Money productPrice;

    @Column(name = "brand_name", nullable = false)
    private String brandName;

    protected OrderItem() {}

    public OrderItem(Long productId, int quantity, String productName, int productPrice, String brandName) {
        validateProductId(productId);
        validateQuantity(quantity);
        validateProductName(productName);
        validateBrandName(brandName);

        this.productId = productId;
        this.quantity = quantity;
        this.productName = productName;
        this.productPrice = new Money(productPrice);
        this.brandName = brandName;
    }

    void assignOrder(Order order) {
        this.order = order;
    }

    private void validateProductId(Long productId) {
        if (productId == null || productId <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "상품 ID는 양수여야 합니다.");
        }
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "수량은 0보다 커야 합니다.");
        }
    }

    private void validateProductName(String productName) {
        if (productName == null || productName.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "상품 이름은 비어있을 수 없습니다.");
        }
    }

    private void validateBrandName(String brandName) {
        if (brandName == null || brandName.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "브랜드 이름은 비어있을 수 없습니다.");
        }
    }

    public Order getOrder() {
        return order;
    }

    public Long getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getProductName() {
        return productName;
    }

    public int getProductPrice() {
        return productPrice.getAmount();
    }

    public String getBrandName() {
        return brandName;
    }
}
