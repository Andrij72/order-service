# 📦 Order Service

Order Service is a microservice responsible for managing orders: creating, updating, and deleting.  
It communicates with other services and infrastructure as follows:

- **OrderService ↔ NotificationService** – asynchronous communication via **Apache Kafka** (Avro format, schema registry).
- **OrderService ↔ InventoryService** – synchronous communication via **REST API** using **Feign client** and Resilience4j.

---

## 🚀 Features

- Create new orders
- Update existing orders
- Delete orders
- Publish and consume events via Kafka (Avro)
- Integration tests for endpoints and service logic
---

## 🛠️ Tech Stack

- **Java 21**
- **Spring Boot 3**
- **Spring Data JPA** (MySQL)
- **Apache Kafka** (event-driven communication with Avro schema)
- **Feign Client** (Inventory Service integration)
- **Resilience4j** (circuit breaker / fault tolerance)
- **Lombok**
- **Maven**
- **Docker & Docker Compose**

---

## 📂 Project Structure

        order-service/
    ├── .github/workflows       # CI/CD
    ├── docker                  # Docker файли
    │   └── mysql
    │       ├── data
    │       └── init.sql
    ├── docker-compose-examples # Docker Compose приклади
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


---

## 📌 REST API Endpoints

| Method | Endpoint                           | Description                        |
|--------|------------------------------------|------------------------------------|
| POST   | `/api/v1/orders`                   | Create a new order                 |
| GET    | `/api/v1/orders/{orderNumber}`        | Retrieve an order by number        |
| GET    | `/api/v1/orders`                   | Retrieve all orders                |
| PUT    | `/api/v1/orders/{orderNumber}`        | Update an existing order           |
| DELETE | `/api/v1/orders/{orderNumber}`        | Delete an order by number          |
| PATCH  | `/api/v1/orders/{orderNumber}/status` | Update only the status of an order |

#### 📌 Order Status Updates

Order Service now supports order status management. Orders can have the following statuses:
1. [x] PENDING – Order created but not paid yet
2. [x] PAID – Order successfully paid
3. [x] CANCELLED – Order cancelled by user or system
4. [x] FAILED – Payment or processing failed
5. [x] DELIVERING – Order is out for delivery
6. [x] COMPLETED – Order successfully delivered

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

This collection includes the following requests:
- **Create Order** (POST `/api/v1/orders`)
- **Get Order by ID** (GET `/api/v1/orders/{orderNumber}`)
- **Get All Orders** (GET `/api/v1/orders`)
- **Update Order** (PUT `/api/v1/orders/{orderNumber}`)
- **Delete Order** (DELETE `/api/v1/orders/{orderNumber}`)

---
## 🧪 Integration Tests

Order Service includes integration tests to verify API endpoints and service logic:

- **OrderServiceIntegrationTest** – tests REST endpoints using Testcontainers for MySQL and WireMock for Inventory Service.
    - Tests include: creating an order, retrieving an existing order, and handling non-existing orders.
- **OrderServiceKafkaIntegrationTest** – Kafka integration test is prepared but commented out; it will be completed in upcoming versions.

To run the tests:

```bash
./mvnw clean test
```
---

### 👨‍💻 Author

Andrii Kulynych — demo project exploring part of microservice architecture with Spring Boot, Kafka, and Kubernetes

