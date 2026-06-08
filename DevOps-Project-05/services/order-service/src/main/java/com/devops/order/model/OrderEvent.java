package com.devops.order.model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @AllArgsConstructor @NoArgsConstructor
public class OrderEvent {
    private String eventType;   // ORDER_CREATED, ORDER_UPDATED, ORDER_CANCELLED
    private Long orderId;
    private String customerId;
    private String productId;
    private String productName;
    private Integer quantity;
    private BigDecimal totalPrice;
    private String status;
    private LocalDateTime timestamp = LocalDateTime.now();
}
