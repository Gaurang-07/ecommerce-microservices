FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build

# Copy root pom.xml first
COPY pom.xml ./

# Copy shared models
COPY shared-models/ ./shared-models/

# Copy payment service
COPY payment-service/pom.xml ./payment-service/
COPY payment-service/src ./payment-service/src

# Copy other service poms (needed for parent dependency)
COPY order-service/pom.xml ./order-service/
COPY product-service/pom.xml ./product-service/

# Build from root with specific modules
RUN mvn clean package -DskipTests -q -pl shared-models,payment-service

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /build/payment-service/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
