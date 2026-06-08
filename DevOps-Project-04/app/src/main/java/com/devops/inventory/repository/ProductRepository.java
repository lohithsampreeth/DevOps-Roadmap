package com.devops.inventory.repository;

import com.devops.inventory.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory(String category);
    List<Product> findByStatus(Product.Status status);
    List<Product> findByNameContainingIgnoreCase(String name);

    @Query("SELECT DISTINCT p.category FROM Product p")
    List<String> findAllCategories();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.status = 'LOW_STOCK'")
    long countLowStock();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.status = 'OUT_OF_STOCK'")
    long countOutOfStock();
}
