package com.devops.product.model;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
@Entity @Table(name="products") @Data @NoArgsConstructor
public class Product {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false) private String name;
    private String description, category;
    @Column(nullable=false,precision=10,scale=2) private BigDecimal price;
    @Column(nullable=false) private Integer stock;
    private boolean active = true;
}
