package com.akul.microservices.order.service;

import com.akul.microservices.order.model.Order;
import com.akul.microservices.order.model.OrderItem;
import com.akul.microservices.order.model.OrderStatus;
import com.akul.microservices.order.model.UserDetails;
import com.akul.microservices.order.repository.OrderRepository;
import com.akul.microservices.order.stubs.InventoryClientStub;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@Testcontainers
@AutoConfigureWireMock(port = 0)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.flyway.enabled=false",
                "spring.cloud.discovery.enabled=false",
                "eureka.client.enabled=false"
        }
)
class OrderServiceIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private WireMockServer wireMockServer;

    @Autowired
    private PlatformTransactionManager txManager;

    @LocalServerPort
    private int port;

    @Container
    static KafkaContainer kafka =
            new KafkaContainer(DockerImageName.parse("apache/kafka:3.7.0"));

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
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        orderRepository.deleteAll();
        wireMockServer.resetAll();

        InventoryClientStub.stubInventoryCall("Samsung-90", 2, wireMockServer);
        InventoryClientStub.stubInventoryCall("iPhone-15", 1, wireMockServer);
    }

    // ---------------------------------------------------------------------
    // CREATE ORDER
    // ---------------------------------------------------------------------
    @Test
    void shouldSubmitOrder_withMultipleItems() {
        String orderJson = """
        {
          "orderNumber": "6ccc23bd-4661-4a75-8989-d4d56e8d9a57",
          "userDetails": {
            "email": "andrii@example.com",
            "firstName": "Andrii",
            "lastName": "K"
          },
          "items": [
            {"sku": "Samsung-90", "product_name": "Samsung 90", "price": 1200.00, "quantity": 2},
            {"sku": "iPhone-15", "product_name": "iPhone 15", "price": 1500.00, "quantity": 1}
          ],
          "status":"PENDING"
        }
        """;

        given()
                .contentType("application/json")
                .body(orderJson)
                .when()
                .post("/api/v1/orders")
                .then()
                .statusCode(201)
                .body("status", Matchers.is("PENDING"))
                .body("items", Matchers.hasSize(2))
                .body("items[0].sku", Matchers.is("Samsung-90"))
                .body("items[1].sku", Matchers.is("iPhone-15"))
                .body("userDetails.email", Matchers.is("andrii@example.com"));


        verify(getRequestedFor(urlPathEqualTo("/api/v1/inventory"))
                .withQueryParam("sku", equalTo("Samsung-90"))
                .withQueryParam("quantity", equalTo("2")));
        verify(getRequestedFor(urlPathEqualTo("/api/v1/inventory"))
                .withQueryParam("sku", equalTo("iPhone-15"))
                .withQueryParam("quantity", equalTo("1")));


        TransactionTemplate template = new TransactionTemplate(txManager);
        template.execute(status -> {
            List<Order> orders = orderRepository.findAll();
            assertThat(orders).hasSize(1);

            Order savedOrder = orders.getFirst();
            assertThat(savedOrder.getItems()).hasSize(2);
            assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
            return null;
        });

    }

    // ---------------------------------------------------------------------
    // GET ORDER
    // ---------------------------------------------------------------------
    @Test
    void shouldRetrieveExistingOrder() {

        OrderItem orderItem1 = OrderItem.builder()
                .sku("Samsung-90")
                .productName("Samsung 90")
                .price(BigDecimal.valueOf(1500))
                .quantity(2)
                .build();

        OrderItem orderItem2 = OrderItem.builder()
                .sku("iPhone-15")
                .productName("iPhone 15")
                .price(BigDecimal.valueOf(1200))
                .quantity(1)
                .build();

        Order order = Order.builder()
                .orderNumber(UUID.randomUUID().toString())
                .status(OrderStatus.PENDING)
                .userDetails(new UserDetails(
                        "andrii@example.com",
                        "Andrii",
                        "K"
                ))

                .build();

        order.setItems(Arrays.asList(orderItem1, orderItem2));
        orderItem1.setOrder(order);
        orderItem2.setOrder(order);


        orderRepository.save(order);

        given()
                .when()
                .get("/api/v1/orders/" + order.getOrderNumber())
                .then()
                .statusCode(200)
                .body("orderNumber", Matchers.is(order.getOrderNumber()))
                .body("items", Matchers.hasSize(2))
                .body("userDetails.firstName", Matchers.is("Andrii"));
    }
    // ---------------------------------------------------------------------
    // NOT FOUND
    // ---------------------------------------------------------------------
    @Test
    void shouldReturn404ForNonExistingOrder() {
        given()
                .when()
                .get("/api/v1/orders/not-exist")
                .then()
                .statusCode(404);
    }
}
