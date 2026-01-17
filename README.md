
---
# 🧾📦 Order Service

**Order Service** is a microservice responsible for managing customer orders in a distributed system.
It handles order creation, retrieval, update, status changes, and deletion. 

The service supports **pagination**, **sorting**, and filtering by status or user email.
It is part of **MicroserviceGrid** project and communicates with other services and infrastructure as follows:

* OrderService ↔ NotificationService – asynchronous communication via Apache Kafka (Avro, schema registry)

* OrderService ↔ InventoryService – synchronous communication via REST API using Resilience4j and CircuitBreaker

---

## 🛠️ Tech Stack

- **Java 21 / Spring Boot 3**
- **Spring Web**
- **Spring Data JPA**
- **MySQL**
- **Apache Kafka** (event-driven communication with other services)
- **Resilience4J** (CircuitBreaker / RateLimiter)
- **Docker / Docker Hub**
- **Testcontainers** (Integration Tests)
- **Spring Validation (jakarta.validation)**

---
## 🌈 Order Service Data Flow

        🌐 API Gateway
        ┌───────────────┐
        │  REST / Web   │
        └───────┬───────┘
                │
                ▼
        🟦 Order Service
        ┌─────────────────────┐
        │ - REST CRUD         │
        │ - Kafka Producer    │
        │ - Inventory REST    │
        │ - DB: MySQL         │
        │ - Resilience4j      │
        └───────┬────────────┘
                │
        ┌───────▼────────┐
        🔹 Inventory Service
        ┌─────────────────┐
        │ REST API         │
        │ Stock Management │
        └─────────┬───────┘
                  │
                  ▼
        🟪 Notification Service
        ┌─────────────────┐
        │ Kafka Consumer   │
        │ Email / Viber    │
        └─────────────────┘

### 🔹 Legend

    🟦 Order Service – this service
    
    🔹 Inventory Service – stock availability check (synchronous REST)
    
    🟪 Notification Service – asynchronous notifications via Kafka
    
    🌐 API Gateway – central entry point (Spring Cloud Gateway / WebFlux)
    
### 🔄 Data Flows

* REST → API Gateway → Order Service
* Synchronous REST → Inventory Service (protected by Resilience4j CircuitBreaker)
* Kafka Events → Notification Service 
* Database → persistence of orders and user details (MySQL) 
* Monitoring / Observability → Prometheus / Grafana / Loki / Tempo
---

📂 Project Structure
```
    order-service/
    ├── .github/workflows       # CI/CD
    ├── docker                  # Docker та MySQL
    │   └── mysql
    │       ├── data
    │       └── init.sql
    ├── docker-compose-examples # Docker Compose для локальної та продакшн збірки
    ├── src
    │   ├── main
    │   │   ├── java/com/akul/microservices/order
    │   │   │   ├── client
    │   │   │   ├── controller
    │   │   │   ├── dto
    │   │   │   ├── event
    │   │   │   ├── exception
    │   │   │   ├── kafka
    │   │   │   ├── mappers
    │   │   │   ├── model
    │   │   │   ├── repository
    │   │   │   └── service
    │   │   └── resources
    │   │       ├── avro
    │   │       ├── db/migration
    │   │       ├── static
    │   │       └── templates
    │   └── test
    │       ├── java/com/akul/microservices/order
    │       │   ├── service
    │       │   └── stubs
    │       └── resources/avro
    ├── pom.xml
    └── Dockerfile
````

## 🧩 Endpoints Overview

| Method | Endpoint | Description | Query / Body |
|--------|---------|-------------|--------------|
| `POST` | `/api/v1/orders` | Create a new order | Body: `OrderRequest` |
| `GET` | `/api/v1/orders/{orderNumber}` | Retrieve a single order by number | Path: `orderNumber` |
| `GET` | `/api/v1/orders` | Retrieve all orders (paginated, sortable, filterable) | Query: `page`, `size`, `sort`, `status`, `email` |
| `PUT` | `/api/v1/orders/{orderNumber}` | Update a full order | Path: `orderNumber`, Body: `OrderRequest` |
| `PATCH` | `/api/v1/orders/{orderNumber}/status` | Update order status only | Path: `orderNumber`, Body: `UpdateOrderStatusRequest` |
| `DELETE` | `/api/v1/orders/{orderNumber}` | Delete an order | Path: `orderNumber` |

---

## 🧰 DTOs

### `OrderRequest`

```json
{
  "userDetails": {
    "email": "andrii@example.com",
    "firstName": "Andrii",
    "lastName": "K"
  },
  "items": [
    {"sku": "Samsung-90", "product_name": "Samsung 90", "price": 1200.0, "quantity": 2},
    {"sku": "iPhone-15", "product_name": "iPhone 15", "price": 1500.0, "quantity": 1}
  ],
  "status": "PENDING"
}
```
### `UpdateOrderStatusRequest`
```json
{
  "status": "COMPLETED"
}
```

### `PageRequestDto`

| Field  | Type         | Description                                                |
| ------ | ------------ | ---------------------------------------------------------- |
| `page` | int          | Page number (default 0)                                    |
| `size` | int          | Page size (default 10)                                     |
| `sort` | List<String> | Optional sorting, e.g., `["createdAt,desc", "status,asc"]` |

### `PageResponseDto<T>`

| Field           | Type    | Description                 |
| --------------- | ------- | --------------------------- |
| `content`       | List<T> | List of items for this page |
| `page`          | int     | Current page number         |
| `size`          | int     | Page size                   |
| `totalElements` | long    | Total number of items       |
| `totalPages`    | int     | Total pages                 |
| `last`          | boolean | Is this the last page?      |

---
 ## 🔹Examples
 ### Create Order
````bash
POST /api/v1/orders
Content-Type: application/json

{
"userDetails": {
"email": "andrii@example.com",
"firstName": "Andrii",
"lastName": "K"
},
"items": [
{"sku": "Samsung-90", "product_name": "Samsung 90", "price": 1200.0, "quantity": 2},
{"sku": "iPhone-15", "product_name": "iPhone 15", "price": 1500.0, "quantity": 1}
],
"status": "PENDING"
}
````
 Response:
```json
{
"orderNumber": "123e4567-e89b-12d3-a456-426614174000",
"userDetails": {
"email": "andrii@example.com",
"firstName": "Andrii",
"lastName": "K"
},
"items": [...],
"status": "PENDING",
"createdAt": "2026-01-17T16:24:17Z"
}
```

### Get All Orders with Pagination, Sorting, Filtering
```bash
GET /api/v1/orders?page=0&size=10&sort=createdAt,desc&status=PENDING&email=andrii@example.com
```
Response:
```json
{
  "content": [...],
  "page": 0,
  "size": 10,
  "totalElements": 5,
  "totalPages": 1,
  "last": true
}
```
---
▶️ Running Locally

#### Step 1: *Clone the repositories*


```bash
git clone https://github.com/Andrij72/order-service.git
git clone https://github.com/Andrij72/inventory-service.git
```
#### Step 2: *Start local infrastructure*

To run Order Service locally, you need MySQL, Kafka, and Inventory Service. Use the provided *docker-compose* examples:

    docker-compose-examples/
    ├── docker-compose.local.yml       # Local: MySQL + Kafka
    ├── docker-compose.override.yml    # Local override: Order Service + MySQL + Kafka
    ├── docker-compose.dockerfile.yml  # Build local Docker image
    └── docker-compose.prod.yml        # Production-ready Docker images( Order Service relese + MySQL + Kafka)



*Option A* — Docker Compose Override (recommended; runs service + Kafka + MySQL in one network):
```bash
docker-compose -f docker-compose-examples/docker-compose.override.yml up --build
```

*Option B* — Local Dockerfile (build image locally and run):
```bash 
docker-compose.local.yml up --build
docker-compose -f docker-compose-examples/docker-compose.dockerfile.yml up --build
````

*Option C* — IntelliJ Run
```bash 
docker-compose.local.yml
``` 
Open project in IntelliJ

#### Step 3 — *Test the REST API*

Use Postman or curl after services are up:

    GET	/api/v1/orders # Retrieve an order by number
-----
## 📌 Testing Endpoints

You can test the Order Service endpoints using Postman.  
Import the Postman collection from the project root:
```
.\Microservices order-service.postman_collection.json
```

---
## 🧪 Integration Tests

Order Service includes integration tests to verify API endpoints and service logic:

- **OrderServiceIntegrationTest** – tests REST endpoints using Testcontainers for MySQL and WireMock for Inventory Service.
- **OrderServiceKafkaIntegrationTest** – Kafka integration test is prepared but commented out; it will be completed in upcoming versions.

To run the tests:

```bash
./mvnw clean test
```
---
## 🌍 Purpose

This service demonstrates:
* Clean microservice architecture
* CRUD operations for orders
* Pagination, sorting, filtering
* Event-driven communication via Kafka
* Validation and REST best practices
* Integration testing with Testcontainers
---
### 👨‍💻 Author
Andrii Kulynch

📅 Version: 2.0