package com.devops.notification.kafka;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component @RequiredArgsConstructor @Slf4j
public class OrderEventConsumer {
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order.created", groupId = "notification-service")
    public void onOrderCreated(String message) {
        try {
            JsonNode event = objectMapper.readTree(message);
            log.info("[NOTIFY] New order {} for customer {} — sending confirmation",
                event.get("orderRef").asText(), event.get("customerId").asText());
        } catch (Exception e) {
            log.error("Error processing order.created", e);
        }
    }

    @KafkaListener(topics = "order.status.updated", groupId = "notification-service")
    public void onOrderStatusUpdated(String message) {
        try {
            JsonNode event = objectMapper.readTree(message);
            log.info("[NOTIFY] Order {} → status: {}", event.get("orderRef").asText(), event.get("newStatus").asText());
        } catch (Exception e) {
            log.error("Error processing order.status.updated", e);
        }
    }
}
