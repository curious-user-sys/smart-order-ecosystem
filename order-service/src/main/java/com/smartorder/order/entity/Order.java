package com.smartorder.order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Farklı mikroservisteki ürünün kimliği; servisler arası gevşek bağlılık için ID tutuyoruz
    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer quantity;

    // Sipariş anındaki birim fiyat (ürün fiyatı sonradan değişse bile sipariş tutarı korunur)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    // İş kuralı: unitPrice * quantity çarpımı servis katmanında hesaplanıp buraya yazılır
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;

    // Enum'u veritabanında ORDINAL (0, 1) yerine STRING ("CREATED") tutarak okunabilirliği koruyoruz
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public Order() {
    }

    public Order(
            Long productId,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal totalPrice,
            OrderStatus status,
            Instant createdAt
    ) {
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}