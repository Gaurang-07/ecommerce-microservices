FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build

# Copy root pom.xml first
COPY pom.xml ./

# Copy shared models
COPY shared-models/ ./shared-models/

# Copy order service
COPY order-service/pom.xml ./order-service/
COPY order-service/src ./order-service/src

# Copy other service poms (needed for parent dependency)
COPY product-service/pom.xml ./product-service/
COPY payment-service/pom.xml ./payment-service/

# Build from root with specific modules
RUN mvn clean package -DskipTests -q -pl shared-models,order-service

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /build/order-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
