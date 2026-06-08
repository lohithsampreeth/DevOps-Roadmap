# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Production
FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache curl dumb-init && \
    addgroup -S appgroup && adduser -S appuser -G appgroup
ARG SERVICE_NAME
LABEL org.opencontainers.image.title="${SERVICE_NAME}"
WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar
RUN chown -R appuser:appgroup /app
USER appuser
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/health || exit 1
ENTRYPOINT ["dumb-init","--"]
CMD ["java","-XX:+UseContainerSupport","-XX:MaxRAMPercentage=75.0","-jar","app.jar"]
