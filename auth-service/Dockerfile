FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build

COPY pom.xml ./
COPY shared-models/ ./shared-models/

# Auth service source
COPY auth-service/pom.xml ./auth-service/
COPY auth-service/src ./auth-service/src

# Other modules' pom.xml — Maven needs these to resolve parent
COPY order-service/pom.xml ./order-service/
COPY payment-service/pom.xml ./payment-service/
COPY product-service/pom.xml ./product-service/
COPY api-gateway/pom.xml ./api-gateway/

RUN mvn clean package -DskipTests -q -pl shared-models,auth-service

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /build/auth-service/target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]