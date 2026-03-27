package com.akul.microservices.order.service;

import com.akul.microservices.order.domain.model.Order;
import com.akul.microservices.order.infrastructure.outbox.OrderOutboxRepository;
import com.akul.microservices.order.infrastructure.persistence.OrderRepository;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(KafkaTestConfig.class)
class OrderServiceIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderOutboxRepository orderOutboxRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        orderOutboxRepository.deleteAll();
        io.restassured.RestAssured.port = port;
        io.restassured.RestAssured.baseURI = "http://localhost";
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
