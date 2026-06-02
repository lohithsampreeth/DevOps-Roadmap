# ============================================================
# Multi-Stage Dockerfile - Java Spring Boot
# Stage 1: Build with Maven
# Stage 2: Lean production image (JRE only)
# ============================================================

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests=false -B

# Stage 2: Production
FROM eclipse-temurin:17-jre-alpine AS production

ARG BUILD_DATE
ARG BUILD_VERSION
ARG GIT_COMMIT

LABEL org.opencontainers.image.title="DevOps Task Manager"
LABEL org.opencontainers.image.version="${BUILD_VERSION}"
LABEL org.opencontainers.image.created="${BUILD_DATE}"
LABEL org.opencontainers.image.revision="${GIT_COMMIT}"

RUN apk add --no-cache curl dumb-init
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar
RUN chown -R appuser:appgroup /app

USER appuser
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["dumb-init", "--"]
CMD ["java", \
     "-XX:+UseContainerSupport", \
     "-XX:MaxRAMPercentage=75.0", \
     "-Djava.security.egd=file:/dev/./urandom", \
     "-jar", "app.jar"]
