package com.devops;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class InventoryServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }
}

// ── Model ─────────────────────────────────────────────────────
@Entity
@Table(name = "inventory")
@Data
@NoArgsConstructor
class InventoryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String productId;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private Integer availableQuantity;

    @Column(nullable = false)
    private Integer reservedQuantity = 0;

    private String warehouse;
    private LocalDateTime lastUpdated = LocalDateTime.now();

    public Integer getTotalQuantity() {
        return availableQuantity + reservedQuantity;
    }
}

// ── Repository ────────────────────────────────────────────────
@Repository
interface InventoryRepository extends JpaRepository<InventoryItem, Long> {
    InventoryItem findByProductId(String productId);

    @Query("SELECT i FROM InventoryItem i WHERE i.availableQuantity <= 10")
    List<InventoryItem> findLowStockItems();
}

// ── Kafka Event (shared DTO) ───────────────────────────────────
@Data
class OrderEvent {
    private String eventType;
    private String orderNumber;
    private String productId;
    private Integer quantity;
    private String customerId;
    private String status;
    private LocalDateTime timestamp;
}

// ── Service ───────────────────────────────────────────────────
@Service
@RequiredArgsConstructor
@Slf4j
class InventoryService {

    private final InventoryRepository repo;

    public List<InventoryItem> getAll() { return repo.findAll(); }

    public InventoryItem getByProductId(String productId) {
        return repo.findByProductId(productId);
    }

    public List<InventoryItem> getLowStock() {
        return repo.findLowStockItems();
    }

    public InventoryItem upsert(InventoryItem item) {
        item.setLastUpdated(LocalDateTime.now());
        return repo.save(item);
    }

    public boolean reserveStock(String productId, int quantity) {
        InventoryItem item = repo.findByProductId(productId);
        if (item == null || item.getAvailableQuantity() < quantity) {
            log.warn("Insufficient stock for product: {} (requested: {})", productId, quantity);
            return false;
        }
        item.setAvailableQuantity(item.getAvailableQuantity() - quantity);
        item.setReservedQuantity(item.getReservedQuantity() + quantity);
        item.setLastUpdated(LocalDateTime.now());
        repo.save(item);
        log.info("Reserved {} units of product {}", quantity, productId);
        return true;
    }

    public void releaseStock(String productId, int quantity) {
        InventoryItem item = repo.findByProductId(productId);
        if (item != null) {
            item.setReservedQuantity(Math.max(0, item.getReservedQuantity() - quantity));
            item.setAvailableQuantity(item.getAvailableQuantity() + quantity);
            item.setLastUpdated(LocalDateTime.now());
            repo.save(item);
            log.info("Released {} units of product {}", quantity, productId);
        }
    }

    // ── Kafka Consumer: listens for order events ──────────
    @KafkaListener(topics = "order-events", groupId = "inventory-service")
    public void handleOrderEvent(OrderEvent event) {
        log.info("Received order event: {} for product: {}", event.getEventType(), event.getProductId());

        switch (event.getEventType()) {
            case "ORDER_CREATED" ->
                reserveStock(event.getProductId(), event.getQuantity());
            case "ORDER_STATUS_UPDATED" -> {
                if ("CANCELLED".equals(event.getStatus())) {
                    releaseStock(event.getProductId(), event.getQuantity() != null ? event.getQuantity() : 0);
                }
            }
            default -> log.debug("Unhandled event type: {}", event.getEventType());
        }
    }
}

// ── REST Controller ───────────────────────────────────────────
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
class InventoryController {

    private final InventoryService service;

    @GetMapping
    public ResponseEntity<List<InventoryItem>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<InventoryItem> getByProduct(@PathVariable String productId) {
        return ResponseEntity.ok(service.getByProductId(productId));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryItem>> getLowStock() {
        return ResponseEntity.ok(service.getLowStock());
    }

    @PostMapping
    public ResponseEntity<InventoryItem> upsert(@RequestBody InventoryItem item) {
        return ResponseEntity.ok(service.upsert(item));
    }

    @PostMapping("/reserve")
    public ResponseEntity<Map<String, Object>> reserve(@RequestParam String productId,
                                                        @RequestParam int quantity) {
        boolean success = service.reserveStock(productId, quantity);
        return ResponseEntity.ok(Map.of("success", success, "productId", productId, "quantity", quantity));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("{\"status\":\"UP\",\"service\":\"inventory-service\"}");
    }
}
