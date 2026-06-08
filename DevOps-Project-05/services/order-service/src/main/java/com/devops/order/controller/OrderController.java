package com.devops.order.controller;
import com.devops.order.model.Order;
import com.devops.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService service;

    @GetMapping
    public ResponseEntity<List<Order>> getAll() { return ResponseEntity.ok(service.getAllOrders()); }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOne(@PathVariable Long id) { return ResponseEntity.ok(service.getById(id)); }

    @PostMapping
    public ResponseEntity<Order> create(@RequestBody Order order) { return ResponseEntity.ok(service.createOrder(order)); }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Order> updateStatus(@PathVariable Long id, @RequestParam Order.Status status) {
        return ResponseEntity.ok(service.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> stats() {
        return ResponseEntity.ok(Map.of(
            "total", service.totalOrders(),
            "pending", service.pendingOrders(),
            "confirmed", service.confirmedOrders()
        ));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "order-service"));
    }
}
