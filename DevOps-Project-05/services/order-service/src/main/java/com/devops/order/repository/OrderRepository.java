package com.devops.order.repository;
import com.devops.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerId(String customerId);
    List<Order> findByStatus(Order.Status status);
}
