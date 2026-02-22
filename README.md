# Kafka-Based E-Commerce Microservices System

A production-ready event-driven microservices architecture for e-commerce built with Spring Boot, Apache Kafka, PostgreSQL, and Redis.

## Architecture Overview

The system consists of three main microservices:

### 1. **Order Service** (Port: 8081)
- Handles order creation and management
- Publishes `OrderCreatedEvent` to Kafka
- Manages order status updates
- Caches orders in Redis

### 2. **Product Service** (Port: 8082)
- Manages product catalog
- Implements Redis-based caching for products
- Handles stock management
- Supports product CRUD operations

### 3. **Payment Service** (Port: 8083)
- Consumes `OrderCreatedEvent` from Kafka
- Processes payments asynchronously
- Publishes `PaymentProcessedEvent` to Kafka
- Integrates with mock payment gateway

## Technology Stack

- **Language**: Java 17
- **Framework**: Spring Boot 3.2.0
- **Message Broker**: Apache Kafka 7.4.0
- **Database**: PostgreSQL 16
- **Cache**: Redis 7
- **Containerization**: Docker & Docker Compose
- **Build Tool**: Maven

## Prerequisites

- Docker and Docker Compose installed
- Java 17+ (for local development)
- Maven 3.9+ (for local development)
- Git

## Project Structure

```
ecommerce-system/
├── shared-models/              # Shared DTOs and Events
│   └── src/main/java/com/ecommerce/shared/
│       ├── dto/               # Data Transfer Objects
│       └── events/            # Kafka Event Models
├── order-service/              # Order Microservice
│   ├── src/main/java/com/ecommerce/order/
│   │   ├── model/             # Order entity
│   │   ├── repository/        # Data access layer
│   │   ├── service/           # Business logic
│   │   └── controller/        # REST endpoints
│   └── Dockerfile
├── product-service/            # Product Microservice
│   ├── src/main/java/com/ecommerce/product/
│   │   ├── model/             # Product entity
│   │   ├── repository/        # Data access layer
│   │   ├── service/           # Business logic (includes Redis caching)
│   │   └── controller/        # REST endpoints
│   └── Dockerfile
├── payment-service/            # Payment Microservice
│   ├── src/main/java/com/ecommerce/payment/
│   │   ├── model/             # Payment entity
│   │   ├── repository/        # Data access layer
│   │   ├── service/           # Business logic + Kafka listener
│   │   ├── config/            # Kafka consumer/producer config
│   │   └── controller/        # REST endpoints
│   └── Dockerfile
├── docker-compose.yml          # Container orchestration
├── init-db.sql                 # Database initialization script
└── pom.xml                     # Maven parent POM

```

## Quick Start

### 1. Clone the Repository
```bash
cd d:\project
```

### 2. Build All Services
```bash
# Build the parent project and all modules
mvn clean install -DskipTests
```

### 3. Start All Containers
```bash
docker-compose up -d --build
```

This will start:
- PostgreSQL Database (Port: 5432)
- Redis Cache (Port: 6379)
- Zookeeper (Port: 2181)
- Kafka Broker (Ports: 9092, 29092)
- Kafka UI (Port: 8080)
- Order Service (Port: 8081)
- Product Service (Port: 8082)
- Payment Service (Port: 8083)

### 4. Verify Services are Running
```bash
docker-compose ps
```

## API Endpoints

### Order Service (http://localhost:8081/api/orders)

#### Create Order
```bash
POST /api/orders
Content-Type: application/json

{
  "customerId": "cust-001",
  "totalAmount": 1029.97,
  "productId": "prod-001",
  "quantity": 1
}
```

#### Get Order
```bash
GET /api/orders/{orderId}
```

#### Get Customer Orders
```bash
GET /api/orders/customer/{customerId}
```

#### Update Order Status
```bash
PUT /api/orders/{orderId}/status?status=PAID
```

### Product Service (http://localhost:8082/api/products)

#### Create Product
```bash
POST /api/products?name=Tablet&description=10-inch%20tablet&price=499.99&stockQuantity=75
```

#### Get Product
```bash
GET /api/products/{productId}
```

#### List All Products
```bash
GET /api/products
```

#### Update Product
```bash
PUT /api/products/{productId}?name=Updated%20Name&description=Updated%20Desc&price=599.99&stockQuantity=100
```

#### Decrease Stock
```bash
PUT /api/products/{productId}/stock?quantity=5
```

### Payment Service (http://localhost:8083/api/payments)

#### Process Payment
```bash
POST /api/payments?orderId={orderId}&amount=1029.97
```

#### Get Payment
```bash
GET /api/payments/{paymentId}
```

#### Get Payment by Order ID
```bash
GET /api/payments/order/{orderId}
```

## Event-Driven Flow

1. **Order Creation**: When a customer creates an order, the Order Service publishes an `OrderCreatedEvent`
2. **Payment Processing**: Payment Service listens for `OrderCreatedEvent` and automatically processes payment
3. **Payment Completion**: Payment Service publishes `PaymentProcessedEvent` after processing
4. **Order Completion**: Order Service can listen for `PaymentProcessedEvent` to complete the order

### Kafka Topics
- `order-created-topic`: OrderCreatedEvent
- `payment-processed-topic`: PaymentProcessedEvent

## Monitoring and Administration

### Kafka UI
Access Kafka UI at: http://localhost:8080

Here you can:
- View all topics and partitions
- Monitor message flow
- View consumer group status
- Inspect message content

### Database Access
```bash
# PostgreSQL
docker-compose exec postgres psql -U ecommerce_user -d ecommerce_db

# Redis CLI
docker-compose exec redis redis-cli
```

### View Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f order-service
docker-compose logs -f product-service
docker-compose logs -f payment-service
```

## Data Persistence

- **PostgreSQL**: All service data persisted in `postgres_data` volume
- **Redis**: Cache data in `redis_data` volume
- **Kafka**: Message data in `kafka_data` volume

## Configuration

### Service Properties

Each service has its own `application.properties` file:
- [order-service/src/main/resources/application.properties](order-service/src/main/resources/application.properties)
- [product-service/src/main/resources/application.properties](product-service/src/main/resources/application.properties)
- [payment-service/src/main/resources/application.properties](payment-service/src/main/resources/application.properties)

### Environment Variables

Customize via Docker Compose environment variables:
- `KAFKA_BOOTSTRAP_SERVERS`: Kafka connection string
- `spring.datasource.*`: Database configuration
- `spring.data.redis.*`: Redis configuration

## Development

### Local Development (Without Docker)

1. **Start infrastructure only**:
```bash
docker-compose up -d postgres redis kafka zookeeper
```

2. **Run services locally** (requires Java 17+):
```bash
# Terminal 1: Order Service
cd order-service
mvn spring-boot:run

# Terminal 2: Product Service
cd product-service
mvn spring-boot:run

# Terminal 3: Payment Service
cd payment-service
mvn spring-boot:run
```

Update `application.properties` to use `localhost` instead of container names:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/ecommerce_db
spring.data.redis.host=localhost
kafka.bootstrap-servers=localhost:9092
```

### Building Docker Images

```bash
# Build all services
docker-compose build

# Build specific service
docker-compose build order-service
```

## Testing

### Sample Workflow

1. **Create a product**:
```bash
curl -X POST "http://localhost:8082/api/products?name=Laptop&description=Gaming%20Laptop&price=1299.99&stockQuantity=100"
```

2. **Create an order**:
```bash
curl -X POST http://localhost:8081/api/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId":"customer-001","totalAmount":1299.99,"productId":"prod-001","quantity":1}'
```

3. **Verify payment was processed** (check logs or query):
```bash
docker-compose logs payment-service | grep "Payment processed"
```

4. **Check order status**:
```bash
curl http://localhost:8081/api/orders/{orderId}
```

## Stopping Services

```bash
# Stop all containers
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

## Performance Considerations

- **Redis Caching**: Products are cached with 1-hour expiration
- **Kafka Partitions**: Distributed message processing for scalability
- **Connection Pooling**: HikariCP with Spring Boot
- **Database Indexes**: Optimized queries on frequently accessed fields

## Troubleshooting

### Services not connecting
```bash
# Check network
docker network ls

# Verify service connectivity
docker-compose exec order-service ping kafka
docker-compose exec payment-service ping postgres
```

### Kafka connection issues
```bash
# Check Kafka logs
docker-compose logs kafka

# Verify topics
docker-compose exec kafka kafka-topics --list --bootstrap-server localhost:9092
```

### Database connection issues
```bash
# Check PostgreSQL logs
docker-compose logs postgres

# Verify database exists
docker-compose exec postgres psql -U ecommerce_user -l
```

### Clear all data and restart
```bash
docker-compose down -v
docker-compose up -d --build
```

## Architecture Characteristics

✅ **Event-Driven**: Loose coupling via Kafka events
✅ **Scalable**: Services can be scaled independently
✅ **Resilient**: Retry mechanisms and error handling
✅ **Persistent**: Database and cache layers
✅ **Observable**: Detailed logging and Kafka UI

## Future Enhancements

- [ ] Service discovery (Eureka/Consul)
- [ ] API Gateway (Spring Cloud Gateway)
- [ ] Circuit Breaker (Resilience4j)
- [ ] Distributed Tracing (Sleuth/Zipkin)
- [ ] Authentication & Authorization (OAuth2/JWT)
- [ ] Notification Service (Email/SMS)
- [ ] Analytics Service
- [ ] Unit and Integration Tests

## License
MIT

## Support
For issues or questions, refer to the documentation or create an issue.
