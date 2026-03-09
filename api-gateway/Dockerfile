FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build

# Step 1: Copy root pom — Maven needs this to understand the parent project
COPY pom.xml ./

# Step 2: Copy shared-models fully — gateway might use shared DTOs later
COPY shared-models/ ./shared-models/

# Step 3: Copy api-gateway source — this is what we're actually building
COPY api-gateway/pom.xml ./api-gateway/
COPY api-gateway/src ./api-gateway/src

# Step 4: Copy OTHER services' pom.xml only (no src needed)
# Why? Root pom.xml lists all modules. Maven validates all of them exist
# during the build, even if we're only compiling api-gateway.
COPY order-service/pom.xml ./order-service/
COPY payment-service/pom.xml ./payment-service/
COPY product-service/pom.xml ./product-service/
COPY auth-service/pom.xml ./auth-service/

# Step 5: Build only what we need — shared-models first (dependency), then gateway
RUN mvn clean package -DskipTests -q -pl shared-models,api-gateway

# ── Stage 2: Lean runtime image ──────────────────────────────────────
FROM eclipse-temurin:17-jre
WORKDIR /app

# Only copy the compiled JAR from stage 1 — nothing else
COPY --from=builder /build/api-gateway/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]