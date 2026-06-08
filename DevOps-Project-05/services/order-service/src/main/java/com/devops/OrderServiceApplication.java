package com.devops;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@SpringBootApplication
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}

// ── Model ─────────────────────────────────────────────────────
@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String orderNumber = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

    @Column(nullable = false)
    private String productId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Double totalAmount;

    private String customerId;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum OrderStatus { PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED }
}

// ── Repository ────────────────────────────────────────────────
@Repository
interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerId(String customerId);
    List<Order> findByStatus(Order.OrderStatus status);
}

// ── Kafka Event ───────────────────────────────────────────────
@Data
class OrderEvent {
    private String eventType;       // ORDER_CREATED, ORDER_UPDATED, ORDER_CANCELLED
    private String orderNumber;
    private String productId;
    private Integer quantity;
    private String customerId;
    private String status;
    private LocalDateTime timestamp = LocalDateTime.now();
}

// ── Service ───────────────────────────────────────────────────
@Service
@RequiredArgsConstructor
class OrderService {

    private final OrderRepository repo;
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    private static final String ORDER_TOPIC = "order-events";

    public List<Order> getAllOrders() { return repo.findAll(); }

    public Order getOrder(Long id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Order not found: " + id));
    }

    public Order createOrder(Order order) {
        Order saved = repo.save(order);

        // Publish ORDER_CREATED event to Kafka
        OrderEvent event = new OrderEvent();
        event.setEventType("ORDER_CREATED");
        event.setOrderNumber(saved.getOrderNumber());
        event.setProductId(saved.getProductId());
        event.setQuantity(saved.getQuantity());
        event.setCustomerId(saved.getCustomerId());
        event.setStatus(saved.getStatus().name());

        kafkaTemplate.send(ORDER_TOPIC, saved.getOrderNumber(), event);
        return saved;
    }

    public Order updateStatus(Long id, Order.OrderStatus status) {
        Order order = getOrder(id);
        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        Order updated = repo.save(order);

        // Publish status update event
        OrderEvent event = new OrderEvent();
        event.setEventType("ORDER_STATUS_UPDATED");
        event.setOrderNumber(updated.getOrderNumber());
        event.setStatus(status.name());

        kafkaTemplate.send(ORDER_TOPIC, updated.getOrderNumber(), event);
        return updated;
    }

    public void cancelOrder(Long id) {
        updateStatus(id, Order.OrderStatus.CANCELLED);
    }
}

// ── REST Controller ───────────────────────────────────────────
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
class OrderController {

    private final OrderService service;

    @GetMapping
    public ResponseEntity<List<Order>> getAll() {
        return ResponseEntity.ok(service.getAllOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(service.getOrder(id));
    }

    @PostMapping
    public ResponseEntity<Order> create(@RequestBody Order order) {
        return ResponseEntity.ok(service.createOrder(order));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Order> updateStatus(@PathVariable Long id,
                                               @RequestParam Order.OrderStatus status) {
        return ResponseEntity.ok(service.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        service.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("{\"status\":\"UP\",\"service\":\"order-service\"}");
    }
}
