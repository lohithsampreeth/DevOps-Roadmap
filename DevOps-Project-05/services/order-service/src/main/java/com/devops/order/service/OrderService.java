package com.devops.order.service;
import com.devops.order.model.Order;
import com.devops.order.model.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

@Service @RequiredArgsConstructor @Slf4j
public class OrderService {
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    private static final String TOPIC = "order-events";

    // In-memory store for demo (replace with JPA in production)
    private final Map<Long, Order> orders = new LinkedHashMap<>();
    private long idSeq = 1;

    public List<Order> getAllOrders() { return new ArrayList<>(orders.values()); }

    public Order getById(Long id) {
        return Optional.ofNullable(orders.get(id))
            .orElseThrow(() -> new RuntimeException("Order not found: " + id));
    }

    public Order createOrder(Order order) {
        order.setId(idSeq++);
        order.setStatus(Order.Status.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orders.put(order.getId(), order);

        // Publish event to Kafka
        OrderEvent event = new OrderEvent("ORDER_CREATED", order.getId(),
            order.getCustomerId(), order.getProductId(), order.getProductName(),
            order.getQuantity(), order.getTotalPrice(), order.getStatus().name(),
            LocalDateTime.now());
        kafkaTemplate.send(TOPIC, order.getId().toString(), event);
        log.info("Order created and event published: orderId={}", order.getId());
        return order;
    }

    public Order updateStatus(Long id, Order.Status status) {
        Order order = getById(id);
        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        orders.put(id, order);

        OrderEvent event = new OrderEvent("ORDER_UPDATED", order.getId(),
            order.getCustomerId(), order.getProductId(), order.getProductName(),
            order.getQuantity(), order.getTotalPrice(), order.getStatus().name(),
            LocalDateTime.now());
        kafkaTemplate.send(TOPIC, order.getId().toString(), event);
        log.info("Order status updated: orderId={}, status={}", id, status);
        return order;
    }

    public void deleteOrder(Long id) {
        Order order = getById(id);
        orders.remove(id);
        OrderEvent event = new OrderEvent("ORDER_CANCELLED", order.getId(),
            order.getCustomerId(), order.getProductId(), order.getProductName(),
            order.getQuantity(), order.getTotalPrice(), "CANCELLED", LocalDateTime.now());
        kafkaTemplate.send(TOPIC, order.getId().toString(), event);
    }

    public long totalOrders() { return orders.size(); }
    public long pendingOrders() { return orders.values().stream().filter(o -> o.getStatus() == Order.Status.PENDING).count(); }
    public long confirmedOrders() { return orders.values().stream().filter(o -> o.getStatus() == Order.Status.CONFIRMED).count(); }
}
