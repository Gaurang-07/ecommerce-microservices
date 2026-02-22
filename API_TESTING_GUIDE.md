# E-Commerce Microservices API Testing Guide

## Quick Start Commands

### Start the System
```bash
# Windows
.\scripts.bat start

# Linux/Mac
./scripts.sh start
```

### Stop the System
```bash
# Windows
.\scripts.bat stop

# Linux/Mac
./scripts.sh stop
```

## Testing the Full Workflow

### 1. Create a Product

```bash
curl -X POST "http://localhost:8082/api/products?name=MacBook%20Pro&description=16-inch%20Laptop&price=2499.99&stockQuantity=25"
```

**Response**:
```json
{
  "productId": "abc-123-def",
  "name": "MacBook Pro",
  "description": "16-inch Laptop",
  "price": 2499.99,
  "stockQuantity": 25,
  "createdAt": "2024-02-14T10:30:00"
}
```

Keep the `productId` for the next step.

### 2. Get Product Details

```bash
curl http://localhost:8082/api/products/abc-123-def
```

### 3. Create an Order

```bash
curl -X POST http://localhost:8081/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "customer-john-001",
    "totalAmount": 2499.99,
    "productId": "abc-123-def",
    "quantity": 1
  }'
```

**Response**:
```json
{
  "orderId": "order-xyz-789",
  "customerId": "customer-john-001",
  "totalAmount": 2499.99,
  "status": "PENDING",
  "createdAt": "2024-02-14T10:35:00"
}
```

Keep the `orderId` for the next steps.

### 4. Automatic Payment Processing

*Note: Payment is automatically triggered by the Payment Service listening to Kafka*

The Payment Service will:
1. Receive the OrderCreatedEvent
2. Process the payment (90% success rate in mock)
3. Publish PaymentProcessedEvent

Wait a few seconds for process to complete.

### 5. Check Order Status

```bash
curl http://localhost:8081/api/orders/order-xyz-789
```

Expected status: `PENDING` (will be updated when payment webhook is processed)

### 6. Check Payment Status

```bash
curl http://localhost:8083/api/payments/order/order-xyz-789
```

**Response**:
```json
{
  "paymentId": "payment-abc-123",
  "orderId": "order-xyz-789",
  "amount": 2499.99,
  "status": "SUCCESS",
  "paymentMethod": "CARD",
  "createdAt": "2024-02-14T10:35:05"
}
```

### 7. Decrease Product Stock

```bash
curl -X PUT "http://localhost:8082/api/products/abc-123-def/stock?quantity=1"
```

Response: `"Stock decreased successfully"`

### 8. Get All Products

```bash
curl http://localhost:8082/api/products
```

### 9. Get Customer Orders

```bash
curl http://localhost:8081/api/orders/customer/customer-john-001
```

## Kafka Topics & Monitoring

### View Kafka UI
Open browser: **http://localhost:8080**

### List Kafka Topics
```bash
# Windows
.\scripts.bat kafka-topics

# Linux/Mac
./scripts.sh kafka-topics
```

Expected output:
```
order-created-topic
payment-processed-topic
```

### View Kafka Messages

Using Kafka UI:
1. Go to http://localhost:8080
2. Click on "order-created-topic" to see order events
3. Click on "payment-processed-topic" to see payment events

## Database Queries

### Connect to PostgreSQL
```bash
# Windows
.\scripts.bat shell-postgres

# Linux/Mac
./scripts.sh shell-postgres
```

### Useful SQL Queries

```sql
-- View all orders
SELECT * FROM orders;

-- View customer orders
SELECT * FROM orders WHERE customer_id = 'customer-john-001';

-- View all products
SELECT * FROM products;

-- View all payments
SELECT * FROM payments;

-- View payment status for an order
SELECT p.payment_id, p.order_id, p.status, p.amount 
FROM payments p 
WHERE p.order_id = 'order-xyz-789';

-- Check stock levels
SELECT product_id, name, stock_quantity FROM products;

-- View recent orders
SELECT * FROM orders ORDER BY created_at DESC LIMIT 10;
```

## Redis Cache Verification

### Connect to Redis
```bash
# Windows
.\scripts.bat shell-redis

# Linux/Mac
./scripts.sh shell-redis
```

### Redis Commands

```bash
# See all cache keys
KEYS product:*

# Get a cached product
GET product:abc-123-def

# See all keys
KEYS *

# Monitor cache operations in real-time
MONITOR

# Get cache info
INFO memory
```

## Service Logs

### View All Logs
```bash
# Windows
.\scripts.bat logs

# Linux/Mac
./scripts.sh logs
```

### View Specific Service Logs
```bash
docker-compose logs -f order-service
docker-compose logs -f product-service
docker-compose logs -f payment-service
```

### See recent logs (last 50 lines)
```bash
docker-compose logs --tail=50 order-service
```

## Error Scenarios

### Test Payment Failure

Since payment has 90% success rate, occasionally you'll see:
- Status: `FAILED`
- Check logs: `docker-compose logs -f payment-service`

### Order Without Stock

```bash
curl -X POST http://localhost:8081/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "customer-test",
    "totalAmount": 10000,
    "productId": "product-empty",
    "quantity": 1000
  }'
```

Response: `"Insufficient stock or product not found"`

## Performance Testing

### Create Multiple Orders
```bash
for i in {1..10}; do
  curl -X POST http://localhost:8081/api/orders \
    -H "Content-Type: application/json" \
    -d "{\"customerId\": \"cust-$i\", \"totalAmount\": $((1000 + i*100)), \"productId\": \"prod-001\", \"quantity\": 1}"
done
```

### Monitor Cache Hit Ratio

Fetch the same product multiple times:
```bash
for i in {1..5}; do
  curl http://localhost:8082/api/products/abc-123-def
done
```

Check logs for cache hits vs database hits.

## Cleanup & Reset

### Stop Everything
```bash
# Windows
.\scripts.bat stop

# Linux/Mac
./scripts.sh stop
```

### Complete Reset (Remove All Data)
```bash
# Windows
.\scripts.bat clean

# Linux/Mac
./scripts.sh clean
```

### Restart Everything from Scratch
```bash
# Windows
.\scripts.bat clean
.\scripts.bat start

# Linux/Mac
./scripts.sh clean
./scripts.sh start
```

## Troubleshooting

### Services not responding
```bash
# Check if containers are running
docker-compose ps

# Restart specific service
docker-compose restart payment-service

# View recent errors
docker-compose logs payment-service --tail=20
```

### Kafka connection issues
```bash
# Check Kafka is running
docker-compose logs kafka | head -50

# Test Kafka connectivity
docker-compose exec payment-service telnet kafka 29092
```

### Database connection timeout
```bash
# Check PostgreSQL
docker-compose logs postgres | head -50

# Test connection
docker-compose exec postgres pg_isready
```

### Port conflicts
If ports are already in use, update `docker-compose.yml`:
```yaml
ports:
  - "8081:8081"  # Change 8081 to available port like 8091:8081
```

## Expected Behavior

1. **Order Creation**: Immediately published to Kafka ✓
2. **Payment Processing**: Automatic within 2-3 seconds ✓
3. **Product Caching**: Cached after first access, expires in 1 hour ✓
4. **Event Publishing**: Visible in Kafka UI within seconds ✓
5. **Database Persistence**: All data survives container restart ✓

## Success Indicators

✅ All 3 services running (docker-compose ps shows all UP)
✅ Kafka topics created automatically
✅ Order events appear in Kafka UI
✅ Payment events processed within seconds
✅ Products cached in Redis
✅ All databases tables created
✅ API responses return 200 OK

## Next Steps

- Explore service logs
- Monitor Kafka UI in real-time
- Test edge cases (invalid orders, payment failures)
- Scale services (docker-compose scale payment-service=2)
- Add custom database queries
