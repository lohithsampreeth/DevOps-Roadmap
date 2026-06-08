package com.devops;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class NotificationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}

// ── Notification Record ────────────────────────────────────────
@Data
class Notification {
    private String id;
    private String type;
    private String recipient;
    private String message;
    private String channel;   // EMAIL, SMS, PUSH
    private LocalDateTime sentAt = LocalDateTime.now();
    private boolean delivered = true;
}

// ── Shared Order Event ─────────────────────────────────────────
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

// ── Notification Service ───────────────────────────────────────
@Service
@RequiredArgsConstructor
@Slf4j
class NotificationService {

    // In-memory store for demo (use DB or Redis in prod)
    private final List<Notification> sentNotifications = new ArrayList<>();

    @KafkaListener(topics = "order-events", groupId = "notification-service")
    public void handleOrderEvent(OrderEvent event) {
        log.info("Notification service received: {} for order: {}",
                 event.getEventType(), event.getOrderNumber());

        String message = switch (event.getEventType()) {
            case "ORDER_CREATED" ->
                String.format("Your order %s has been placed successfully! We're processing it now.",
                              event.getOrderNumber());
            case "ORDER_STATUS_UPDATED" ->
                String.format("Order %s status updated to: %s",
                              event.getOrderNumber(), event.getStatus());
            default -> null;
        };

        if (message != null && event.getCustomerId() != null) {
            sendNotification(event.getCustomerId(), message, event.getEventType());
        }
    }

    private void sendNotification(String customerId, String message, String type) {
        // In real world: integrate with SendGrid/Twilio/Firebase
        Notification n = new Notification();
        n.setId("notif-" + System.currentTimeMillis());
        n.setType(type);
        n.setRecipient(customerId);
        n.setMessage(message);
        n.setChannel("EMAIL");
        sentNotifications.add(n);

        log.info("📧 Notification sent to {} | Type: {} | Message: {}", customerId, type, message);
    }

    public List<Notification> getAllNotifications() { return sentNotifications; }

    public void sendManual(String recipient, String message, String channel) {
        Notification n = new Notification();
        n.setId("manual-" + System.currentTimeMillis());
        n.setType("MANUAL");
        n.setRecipient(recipient);
        n.setMessage(message);
        n.setChannel(channel);
        sentNotifications.add(n);
        log.info("📧 Manual notification sent to {}", recipient);
    }
}

// ── REST Controller ───────────────────────────────────────────
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
class NotificationController {

    private final NotificationService service;

    @GetMapping
    public ResponseEntity<List<Notification>> getAll() {
        return ResponseEntity.ok(service.getAllNotifications());
    }

    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> send(
            @RequestParam String recipient,
            @RequestParam String message,
            @RequestParam(defaultValue = "EMAIL") String channel) {
        service.sendManual(recipient, message, channel);
        return ResponseEntity.ok(Map.of("status", "sent", "recipient", recipient));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("{\"status\":\"UP\",\"service\":\"notification-service\"}");
    }
}
