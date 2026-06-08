package com.devops.order.kafka;
import com.devops.order.model.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component @RequiredArgsConstructor @Slf4j
public class OrderEventProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    public static final String ORDER_CREATED_TOPIC = "order.created";
    public static final String ORDER_UPDATED_TOPIC = "order.status.updated";

    public void sendOrderCreated(Order order) {
        sendEvent(ORDER_CREATED_TOPIC, order.getOrderRef(), Map.of(
            "event", "ORDER_CREATED", "orderRef", order.getOrderRef(),
            "customerId", order.getCustomerId(), "productId", order.getProductId(),
            "quantity", order.getQuantity().toString(), "status", order.getStatus().toString()
        ));
    }

    public void sendOrderStatusUpdated(Order order) {
        sendEvent(ORDER_UPDATED_TOPIC, order.getOrderRef(), Map.of(
            "event", "ORDER_STATUS_UPDATED", "orderRef", order.getOrderRef(),
            "customerId", order.getCustomerId(), "newStatus", order.getStatus().toString()
        ));
    }

    private void sendEvent(String topic, String key, Map<String, Object> payload) {
        try {
            kafkaTemplate.send(topic, key, objectMapper.writeValueAsString(payload));
            log.info("Published to {}: key={}", topic, key);
        } catch (Exception e) {
            log.error("Failed to serialize Kafka event", e);
        }
    }
}
