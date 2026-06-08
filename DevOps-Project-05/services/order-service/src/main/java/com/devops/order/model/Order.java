package com.devops.order.model;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "orders") @Data @NoArgsConstructor
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String customerId;
    private String productId;
    private String productName;
    private Integer quantity;
    private BigDecimal totalPrice;
    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    public enum Status { PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED }
}
