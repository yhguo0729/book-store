package com.yf.bookstore.model.order;

import org.springframework.data.annotation.Id;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "tb_order")
public class Order {

    public Order(){}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 主键字段

    @Column(name = "customer_id", nullable = false, length = 255)
    private String customerId; // 客户ID

    @Enumerated(EnumType.ORDINAL)
    private OrderStatus status;

    @Column(name = "sku_id", nullable = false)
    private Long skuId; // SKU ID

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price; // 单价

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount; // 总金额

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime; // 创建时间

    @Column(name = "modify_time", nullable = false)
    private LocalDateTime modifyTime; // 修改时间



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }
}
