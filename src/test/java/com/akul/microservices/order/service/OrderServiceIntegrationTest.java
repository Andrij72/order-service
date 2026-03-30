package com.akul.microservices.order.service;

import com.akul.microservices.order.application.service.OrderService;
import com.akul.microservices.order.domain.model.Order;
import com.akul.microservices.order.infrastructure.outbox.OrderOutboxRepository;
import com.akul.microservices.order.infrastructure.persistence.OrderRepository;
import io.restassured.RestAssured;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@Testcontainers
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.flyway.enabled=false",
                "loki.enabled=false",
                "spring.cloud.discovery.enabled=false",
                "eureka.client.enabled=false",
                "spring.kafka.listener.auto-startup=false",
                "spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer",
                "spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer",
                "spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer",
                "spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer",
                "spring.kafka.consumer.properties.spring.json.trusted.packages=com.akul.microservices.**",
                "spring.task.scheduling.enabled=false",
                "spring.lifecycle.timeout-per-shutdown-phase=5s"
        }
)

class OrderServiceIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderOutboxRepository orderOutboxRepository;

    @Autowired
    private OrderService orderService;

    @Container
    static KafkaContainer kafka =
            new KafkaContainer(
                  DockerImageName.parse("confluentinc/cp-kafka:7.7.8")
           );

    @ServiceConnection
    static MySQLContainer<?> mysql =
            new MySQLContainer<>("mysql:8.3.0")
                    .withDatabaseName("orderdb")
                    .withUsername("test")
                    .withPassword("test")
                    .withInitScript("schema.sql");

    @DynamicPropertySource
    static void registerKafka(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @BeforeEach
    void setUp() {
        if (!kafka.isRunning()) kafka.start();

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        orderRepository.deleteAll();
        orderOutboxRepository.deleteAll();
    }

    @Test
    void shouldPlaceOrder_andCancelOrder() {

        // -------------------------------
        // CREATE ORDER
        // -------------------------------
        String orderNumber = createOrder();

        Order saved = orderRepository.findByOrderNumber(orderNumber).orElseThrow();
        assertThat(saved.getOrderNumber()).isEqualTo(orderNumber);

        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    List<?> events = orderOutboxRepository.findAll();
                    assertThat(events).hasSize(1);
                });

        // -------------------------------
        // CANCEL ORDER
        // -------------------------------
        given()
                .patch("/api/v1/orders/{orderNumber}/cancel", orderNumber)
                .then()
                .statusCode(200);

        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Order cancelled = orderRepository.findByOrderNumber(orderNumber).orElseThrow();
                    assertThat(cancelled.getStatus().name()).isEqualTo("CANCELLED");
                });
    }

    // ---------------------------------------------------------------------
    // HELPER
    // ---------------------------------------------------------------------
    private String createOrder() {

        String orderJson = """
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
        """;

        return given()
                .contentType("application/json")
                .body(orderJson)
                .when()
                .post("/api/v1/orders")
                .then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getString("orderNumber");
    }
}
