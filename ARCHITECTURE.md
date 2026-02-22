# Architecture Documentation

## System Overview

This is a production-ready microservices architecture demonstrating event-driven design patterns using Apache Kafka as the central message broker.

## Core Components

### 1. Event-Driven Messaging
- **Message Broker**: Apache Kafka
- **Topics**: 
  - `order-created-topic`: Triggered when orders are created
  - `payment-processed-topic`: Triggered when payments complete
- **Pattern**: Publish-Subscribe for loose coupling

### 2. Microservices

#### Order Service
- **Port**: 8081
- **Responsibilities**:
  - Create and manage orders
  - Store orders in PostgreSQL
  - Publish OrderCreatedEvent to Kafka
  - Cache order data
- **Database**: `orders` table
- **Key Classes**:
  - `OrderService`: Business logic
  - `OrderController`: REST API
  - `OrderRepository`: Data access

#### Product Service
- **Port**: 8082
- **Responsibilities**:
  - Manage product catalog
  - Implement Redis caching
  - Handle stock management
  - Provide product search
- **Database**: `products` table
- **Caching**: Redis with 1-hour TTL
- **Key Classes**:
  - `ProductService`: Business logic + caching
  - `ProductController`: REST API
  - `ProductRepository`: Data access

#### Payment Service
- **Port**: 8083
- **Responsibilities**:
  - Listen for OrderCreatedEvent
  - Process payments asynchronously
  - Publish PaymentProcessedEvent
  - Integrate with payment gateway
- **Database**: `payments` table
- **Key Classes**:
  - `PaymentService`: Business logic + Kafka listener
  - `PaymentController`: REST API
  - `KafkaConsumerConfig`: Kafka consumer setup
  - `KafkaProducerConfig`: Kafka producer setup

### 3. Data Layer

#### PostgreSQL Database
- **Host**: postgres:5432
- **Database**: ecommerce_db
- **User**: ecommerce_user
- **Tables**:
  - `orders`: Order records
  - `products`: Product catalog
  - `payments`: Payment records
- **Features**: Indexes, foreign keys, timestamps

#### Redis Cache
- **Host**: redis:6379
- **Purpose**: Product caching
- **TTL**: 1 hour for products
- **Pattern**: Cache-aside (lazy loading)

### 4. Infrastructure

#### Zookeeper
- **Host**: zookeeper:2181
- **Purpose**: Kafka coordination and state management

#### Kafka
- **Internal**: kafka:29092
- **External**: localhost:9092
- **UI**: http://localhost:8080
- **Broker ID**: 1
- **Partitions**: Auto-created for new topics

## Communication Patterns

### Synchronous Communication
- Client → Microservice (REST API)
- Microservice → PostgreSQL (JDBC)
- Microservice → Redis (Direct)

### Asynchronous Communication
- Order Service → Kafka (OrderCreatedEvent)
- Payment Service ← Kafka (OrderCreatedEvent)
- Payment Service → Kafka (PaymentProcessedEvent)

## Data Flow Diagram

```
┌──────────────┐
│    Client    │
└──────┬───────┘
       │
       ├─── REST ──────┬──────────────────────┬──────────────┐
       │               │                      │              │
    ┌──▼──┐       ┌────▼──┐            ┌─────▼──┐      ┌───▼────┐
    │Order│       │Product│            │Payment│      │ Shared │
    └──┬──┘       └────┬──┘            └─────┬──┘      └───┬────┘
       │                │                      │              │
       │                │                      │              │
    ┌──▼──────────┐    ┌▼────────┐         ┌──▼──────────┐   │
    │ PostgreSQL  │    │  Redis  │         │ PostgreSQL  │   │
    │   Orders    │    │ Products│         │  Payments   │   │
    └─────────────┘    └─────────┘         └─────────────┘   │
       │                                       │               │
       └────────────┬────────────────────────┬─────────────────┘
                    │
               ┌────▼────┐
               │  Kafka  │
               │ Message │
               │ Broker  │
               └────┬────┘
                    │
         ┌──────────┴──────────┐
         │                     │
    ┌────▼──────┐      ┌──────▼────┐
    │Order Created   │Payment      │
    │Event Topic │    │Processed   │
    │            │    │Topic       │
    └────────────┘    └────────────┘
         ▲                   │
         │                   │
    ┌────┴───────────────────▼──┐
    │   Payment Service          │
    │   (Kafka Listener)         │
    └────────────────────────────┘
```

## Deployment Architecture

```
Docker Network: ecommerce-network

┌─────────────────────────────────────────────────────┐
│                                                     │
│  PostgreSQL Container                              │
│  - Volume: postgres_data                           │
│  - Port: 5432                                      │
│                                                     │
├─────────────────────────────────────────────────────┤
│                                                     │
│  Redis Container                                   │
│  - Volume: redis_data                              │
│  - Port: 6379                                      │
│                                                     │
├─────────────────────────────────────────────────────┤
│                                                     │
│  Zookeeper Container                               │
│  - Port: 2181                                      │
│                                                     │
├─────────────────────────────────────────────────────┤
│                                                     │
│  Kafka Container                                   │
│  - Volume: kafka_data                              │
│  - Internal: 29092, External: 9092                 │
│                                                     │
├─────────────────────────────────────────────────────┤
│                                                     │
│  Kafka UI Container                                │
│  - Port: 8080                                      │
│                                                     │
├─────────────────────────────────────────────────────┤
│                                                     │
│  Order Service Container                           │
│  - Port: 8081                                      │
│  - Image: Built from Dockerfile                    │
│                                                     │
├─────────────────────────────────────────────────────┤
│                                                     │
│  Product Service Container                         │
│  - Port: 8082                                      │
│  - Image: Built from Dockerfile                    │
│                                                     │
├─────────────────────────────────────────────────────┤
│                                                     │
│  Payment Service Container                         │
│  - Port: 8083                                      │
│  - Image: Built from Dockerfile                    │
│                                                     │
└─────────────────────────────────────────────────────┘
```

## Technology Justification

### Spring Boot
- ✅ Rapid application development
- ✅ Built-in embedded server (Tomcat)
- ✅ Auto-configuration
- ✅ Excellent ecosystem

### Kafka
- ✅ High-throughput message broker
- ✅ Distributed and scalable
- ✅ Fault-tolerant with replication
- ✅ Order guarantee per partition

### PostgreSQL
- ✅ ACID compliance
- ✅ Robust and mature
- ✅ Excellent for structured data
- ✅ Full-text search capabilities

### Redis
- ✅ Ultra-fast in-memory cache
- ✅ Simple key-value operations
- ✅ TTL support
- ✅ Minimal configuration overhead

### Docker
- ✅ Consistent environment across machines
- ✅ Service isolation
- ✅ Easy scaling and orchestration
- ✅ Simplified dependency management

## Scalability Considerations

### Horizontal Scaling
1. **Order Service**: Stateless - can run multiple instances behind load balancer
2. **Product Service**: Stateless - cache consistency via Redis
3. **Payment Service**: Kafka consumer groups handle load distribution

### Vertical Scaling
- Increase JVM heap size: `-Xmx2g` in Docker
- Increase Kafka partitions for parallel processing
- PostgreSQL connection pooling (HikariCP)

### Bottleneck Points
1. **Database**: Add read replicas for queries
2. **Redis**: Cluster mode for distributed caching
3. **Kafka**: Increase partitions and consumer threads
4. **Network**: Use local volume mounts for I/O

## Monitoring Strategy

### Application Metrics
- Service startup time
- Request latency (p50, p95, p99)
- Error rates by service
- Active connections

### Infrastructure Metrics
- CPU and memory usage
- Disk I/O
- Network throughput
- Container uptime

### Business Metrics
- Orders created per minute
- Payment success rate
- Cache hit ratio
- Kafka lag

### Tools Available
- **Kafka UI**: http://localhost:8080
- **Docker Stats**: `docker stats`
- **Application Logs**: `docker-compose logs`

## Security Considerations

### Current Implementation (Development)
- ⚠️ No authentication (development-only)
- ⚠️ No HTTPS (Docker internal)
- ⚠️ Hard-coded credentials in compose file

### Production Recommendations
1. **API Security**:
   - Implement OAuth2/JWT
   - API Gateway with rate limiting
   - DDoS protection

2. **Data Security**:
   - Enable SSL/TLS for all connections
   - Database encryption at rest
   - Secrets management (Vault, AWS Secrets Manager)

3. **Network Security**:
   - Network policies
   - Service mesh (Istio)
   - VPC isolation

4. **Monitoring**:
   - Distributed tracing (Jaeger)
   - Security scanning
   - Audit logging

## Disaster Recovery

### Backup Strategy
```
PostgreSQL:
- Continuous WAL archiving
- Backup frequency: Daily
- Retention: 30 days

Kafka:
- Replication factor: 3
- Min in-sync replicas: 2
- Retention: 24 hours
```

### Recovery Procedures
1. Database restore from backup
2. Kafka replay from topic beginning
3. Service restart with initialized state

## Testing Strategy

### Unit Tests
- Service layer logic
- Repository queries
- Event serialization

### Integration Tests
- Kafka pub/sub flow
- Database transactions
- Redis caching behavior

### End-to-End Tests
- Complete workflow from order to payment
- All services running
- Full database and cache

## Performance Benchmarks

### Expected Performance
- Order creation: < 100ms
- Payment processing: < 500ms
- Product lookup (cached): < 10ms
- Kafka message latency: < 100ms

### Load Capacity
- 1000s of orders/minute
- Thousands of concurrent users
- Millions of products in catalog

## Future Enhancements

### Short Term
- [ ] Unit and integration tests
- [ ] API documentation (Swagger/OpenAPI)
- [ ] Input validation and error handling
- [ ] Transactional consistency

### Medium Term
- [ ] API Gateway
- [ ] Service discovery (Eureka)
- [ ] Circuit breaker (Resilience4j)
- [ ] Distributed tracing (Sleuth/Jaeger)

### Long Term
- [ ] GraphQL API
- [ ] Real-time notifications (WebSocket)
- [ ] ML-based recommendations
- [ ] Analytics and BI integration
- [ ] Blockchain for audit trail

## Conclusion

This architecture demonstrates modern microservices best practices:
- ✅ Event-driven design
- ✅ Separation of concerns
- ✅ Scalability
- ✅ Fault tolerance
- ✅ Rich caching strategy
- ✅ Docker containerization

Perfect for learning, prototyping, or as a foundation for production systems.
