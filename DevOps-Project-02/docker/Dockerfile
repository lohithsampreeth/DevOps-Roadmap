# ==============================================
# Stage 1: Builder
# ==============================================
FROM node:20-alpine AS builder

LABEL maintainer="yourname@email.com"
LABEL version="1.0"
LABEL description="DevOps CI/CD Project - Builder Stage"

WORKDIR /app

# Copy dependency files first (layer caching)
COPY app/package*.json ./

# Install all deps including devDependencies for build
RUN npm ci

# Copy source code
COPY app/ .

# Run build
RUN npm run build

# ==============================================
# Stage 2: Production
# ==============================================
FROM node:20-alpine AS production

LABEL maintainer="yourname@email.com"
LABEL version="1.0"
LABEL description="DevOps CI/CD Project - Production Stage"

# Install dumb-init for proper signal handling
RUN apk add --no-cache dumb-init wget

# Create non-root user and group (security best practice)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy only production artifacts from builder
COPY --from=builder /app/dist ./dist
COPY --from=builder /app/node_modules ./node_modules
COPY --from=builder /app/package*.json ./

# Set correct ownership
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose app port
EXPOSE 8080

# Healthcheck - verifies app is running correctly
HEALTHCHECK --interval=30s --timeout=10s --start-period=10s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/health || exit 1

# Use dumb-init to handle signals properly
ENTRYPOINT ["dumb-init", "--"]
CMD ["node", "dist/server.js"]
