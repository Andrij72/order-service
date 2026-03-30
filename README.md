
---
# 🧾 Order Service (Saga Version)
Order Service manages the lifecycle of orders within the [MicroServiceGrid system-DDD](https://github.com/Andrij72/MicroserviceGrid-DDD) and 
is a microservice responsible for managing orders in an e-commerce system.  
This is the **updated Saga version**, replacing the legacy CRUD approach.  
You can find the legacy CRUD version here: [Legacy CRUD Version](https://github.com/Andrij72/order-service/tree/develop_crud)

---

## 📌 Features

- **Order management**: create, update, cancel, and fetch orders.
- **Saga orchestration** using Kafka for reliable business processes:
    - Inventory confirmation
    - Payment processing
- **Outbox pattern** for reliable Kafka event publishing.
- **Validation** via `@Valid` annotations.
- **Custom exceptions** and global handling (`OrderNotFoundException`, `ProductOutOfStockException`, etc.).

---

## ⚙️ Technology Stack

- Java 21
- Spring Boot 3
- Spring Data JPA
- Kafka (Spring Kafka)
- MySQL (Flyway migrations)
- Lombok
- Avro for event serialization

---

## 🛠 Docker

Dockerfile and docker-compose are provided for local development and production deployment.

```bash
# Build Docker image
docker build -t order-service:latest .

# Run locally with docker-compose
docker-compose -f docker-compose.local.yml up
```
---
## 📡 Kafka Integration

The service listens to the following topics:

| Topic               | Description         | Listener Method                            |
|---------------------|---------------------|--------------------------------------------|
| inventory-confirmed | Inventory confirmed | handleInventoryConfirmed(InventoryEvent)   |
| inventory-rejected  | Inventory rejected  | handleInventoryRejected(InventoryEvent)    |
| payment-completed   | Payment completed   | handlePaymentCompleted(String orderNumber) |
| payment-failed      | Payment failed      | handlePaymentFailed(String orderNumber)    |

Events are published using **Outbox**, processed by a Kafka Worker.

---
## 📄 REST API
Base URL: /api/v1/orders

### Create Order
```http
POST /api/v1/orders
Content-Type: application/json

{
"userDetails": {
  "email": "user@example.com",
  "name": "Andrii"
},
"items": [
  { "productId": "123", "quantity": 2 }
]
}
```
**Response**: 201 Created


### Get Order
```http
GET /api/v1/orders/{orderNumber}
```
**Response**: 200 OK

### Get All Orders (pagination + filters)
```http
GET /api/v1/orders?page=0&size=10&sort=createdAt,desc&status=CREATED&email=user@example.com
```

### Update Full Order
```http
PUT /api/v1/orders/{orderNumber}
Content-Type: application/json
```
### Cancel Order
```http
PATCH /api/v1/orders/{orderNumber}/cancel
```

---

## 🗂 Project Structure

```text
  src/main/java/com/akul/microservices/order
  ├─ controller   # REST endpoints
  ├─ application  # DTOs and services
  ├─ domain       # Models, OrderStatus, Exceptions
  ├─ event        # Events (PaymentRequestedEvent)
  ├─ infrastructure
  │   ├─ messaging/kafka  # Kafka Listeners & Topic Resolver
  │   ├─ outbox           # Outbox pattern
  │   ├─ persistence      # JPA repositories
  │   └─ worker           # Outbox Publisher
  └─ resources
  ├─ db/migration     # Flyway SQL migrations
  └─ application*.properties
  ```
---
## 🔄 ## 🔹 Saga Flow (Order Perspective)

```text
     User
      │
      ▼
     Order Service
      │
      ├── validate order
      ├── persist order in DB
      ├── save ORDER_CREATED event to Outbox
      │
      ▼
     Outbox Worker → Kafka
      │
      ▼
     Inventory Service
      ├── check stock
      ├── reserve inventory (per SKU)
      ├── create reservation (TTL)
      ├── save INVENTORY_CONFIRMED / INVENTORY_REJECTED / INVENTORY_EXPIRED to Outbox
      │
      ▼
     Outbox Worker → Kafka
      │
      ▼
     Order Service reacts
      ├── CONFIRMED → publish PAYMENT_REQUESTED
      └── REJECTED / EXPIRED → mark order as FAILED
      │
      ▼
     Payment Service
      ├── process payment
      ├── save PAYMENT_COMPLETED / PAYMENT_FAILED to Outbox
      │
      ▼
     Outbox Worker → Kafka
      │
      ▼
     Order Service updates order status
      ├── PAYMENT_COMPLETED → COMPLETED
      └── PAYMENT_FAILED → FAILED + INVENTORY_CANCELLED
```
---

### ⏳ TTL Expiration Flow
```text
Scheduler
│
├── find expired reservations
├── mark EXPIRED
├── release inventory
├── save INVENTORY_EXPIRED to Outbox
▼
Kafka → Order Service
│
└── update order as FAILED
```

---

🧠 Key Design Principles

* Outbox pattern ensures reliable event delivery
* Idempotent operations via (order_id, sku_code) uniqueness
* TTL reservations prevent stock locking
* No distributed transactions — each service updates its state based on events
* Multi-SKU orders processed independently
---

## 📦 Order States
```text
PENDING
├─> INVENTORY_RESERVED
│    ├─> PAYMENT_PROCESSING
│    │    ├─> PAID
│    │    └─> FAILED
├─> FAILED
└─> CANCELLED
```
---

## 🔑 Key Features
* Create orders via REST API
* Confirm or reject orders through Saga
* Automatic status updates
* Outbox + Kafka for guaranteed event delivery
* Idempotent handling to avoid duplication
---

## 🔗 Public API (OrderController)

| Method | Endpoint                            | Description      |
|--------|-------------------------------------|------------------|
| POST   | /api/v1/orders                      | Create an order  |
| PATCH  | /api/v1/orders/{orderNumber}/cancel | Cancel an order  |
| GET    | /api/v1/orders/{orderNumber}        | Get order status |


Example:
```http request
POST /api/v1/orders
Content-Type: application/json

  {
  "items": [
  { "sku": "iPhone-15", "productName": "iPhone 15", "price": 1500.0, "quantity": 1 }
  ],
  "userDetails": {
  "email": "test@example.com",
  "firstName": "Test",
  "lastName": "User"
  }
  }
```
## 🧪 Tests
* Integration tests via Testcontainers (Kafka + MySQL)
* Verify Saga flow: CREATE → INVENTORY_CONFIRMED → PAYMENT → ORDER_COMPLETED
* Idempotency, Outbox, and TTL handling

---

## 🐳 Running Locally
```shell
git clone https://github.com/Andrij72/order-service.git
cd order-service
docker-compose -f docker-compose.local.yml up -d
./mvnw spring-boot:run
```
---

## ⚠️ Failure Handling
* Inventory insufficient → INVENTORY_REJECTED → order CANCELLED
* Reservation TTL expired → INVENTORY_EXPIRED → order CANCELLED
* Payment failed → PAYMENT_FAILED → compensation: INVENTORY_CANCELLED

---
### 👨‍💻 Author
_**Andrii Kulynch**_
Microservices, Kafka, Saga, Outbox, Spring Boot
📅 Version: 3.0