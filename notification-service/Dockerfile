FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build

COPY pom.xml ./
COPY shared-models/ ./shared-models/

# Notification service source
COPY notification-service/pom.xml ./notification-service/
COPY notification-service/src ./notification-service/src

# Other modules pom.xml — Maven needs these
COPY order-service/pom.xml ./order-service/
COPY product-service/pom.xml ./product-service/
COPY payment-service/pom.xml ./payment-service/
COPY api-gateway/pom.xml ./api-gateway/
COPY auth-service/pom.xml ./auth-service/

RUN mvn clean package -DskipTests -q -pl shared-models,notification-service

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /build/notification-service/target/*.jar app.jar
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]