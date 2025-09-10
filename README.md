# 📦 Order Service

Order Service is a microservice responsible for managing orders: creating, updating, and deleting.

It is part of a microservices architecture and communicates with other services as follows:

- **OrderService ↔ NotificationService** – asynchronous communication via **Apache Kafka**.
- **OrderService ↔ InventoryService** – synchronous communication via **REST API** using **Resilience4j*

---

## 🚀 Features

- Create new orders
- Update existing orders
- Delete orders
- Publish and consume events via Kafka

---

## 🛠️ Tech Stack

- **Java 21** (or Java 17 if required)
- **Spring Boot 3**
- **Spring Data JPA** (MySQL)
- **Apache Kafka** (event-driven communication)
- **Confluent Schema Registry** (if using Avro serialization)
- **Lombok**
- **Maven** (build tool)
- **Docker & Docker Compose** (containerization & infrastructure)

---

## 📂 Project Structure

    order-service/
    ├── src/main/java/com/akul/microservices/order
    │ ├── controller # REST controllers
    │ ├── service # Business logic
    │ ├── repository # Database access
    │ ├── model # Entities (Order, ...)
    │ └── events # Kafka producers/consumers
    ├── src/main/resources
    │ └── application.yml
    ├── pom.xml
    └── Dockerfile

---

## ▶️ Running locally

1. Start infrastructure (MySQL + Kafka + Schema Registry + UI):
   ```bash
   docker-compose up -d

2. Run the application:
   ```bash
   mvn spring-boot:run
