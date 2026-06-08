package com.devops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // Route to Order Service
            .route("order-service", r -> r
                .path("/api/orders/**")
                .filters(f -> f
                    .addRequestHeader("X-Gateway", "devops-gateway")
                    .circuitBreaker(c -> c.setName("order-cb").setFallbackUri("forward:/fallback/orders"))
                )
                .uri("http://order-service:8081"))

            // Route to Inventory Service
            .route("inventory-service", r -> r
                .path("/api/inventory/**")
                .filters(f -> f
                    .addRequestHeader("X-Gateway", "devops-gateway")
                    .circuitBreaker(c -> c.setName("inventory-cb").setFallbackUri("forward:/fallback/inventory"))
                )
                .uri("http://inventory-service:8082"))

            // Route to Notification Service
            .route("notification-service", r -> r
                .path("/api/notifications/**")
                .filters(f -> f.addRequestHeader("X-Gateway", "devops-gateway"))
                .uri("http://notification-service:8083"))
            .build();
    }

    @GetMapping("/health")
    public String health() {
        return "{\"status\":\"UP\",\"service\":\"api-gateway\"}";
    }

    @GetMapping("/fallback/orders")
    public String orderFallback() {
        return "{\"error\":\"Order service temporarily unavailable\",\"status\":503}";
    }

    @GetMapping("/fallback/inventory")
    public String inventoryFallback() {
        return "{\"error\":\"Inventory service temporarily unavailable\",\"status\":503}";
    }
}
