package com.akul.microservices.order.service;

import com.akul.microservices.order.model.Order;
import com.akul.microservices.order.repository.OrderRepository;
import com.akul.microservices.order.stubs.InventoryClientStub;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;

@Testcontainers
@TestPropertySource(locations = "classpath:application-test.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.flyway.enabled=false")
@AutoConfigureWireMock(port = 0)
class OrderServiceIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;


    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.3.0")
            .withDatabaseName("orderdb")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("schema.sql");


    @LocalServerPort
    private Integer port;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        orderRepository.deleteAll();
    }

    static {
        mysql.start();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void shouldSubmitOrder() {
        String submitOrderJson = """
                    {
                      "skuCode": "iphone_15",
                      "price": 1000.0,
                      "quantity": 1
                    }
                """;

        InventoryClientStub.stubInventoryCall("iphone_15", 1);

        given()
                .contentType("application/json")
                .body(submitOrderJson)
                .when()
                .post("/api/v1/orders")
                .then()
                .log().all()
                .statusCode(201)
                //.body(Matchers.startsWith("Order created successfully."));
                .body("skuCode", Matchers.is("iphone_15"))
                .body("price", Matchers.is(1000.0F))
                .body("quantity", Matchers.is(1))
                .body("orderNbr", Matchers.notNullValue());


        var savedOrders = orderRepository.findAll();
        assertThat(savedOrders.size(), Matchers.is(1));
        assertThat(savedOrders.get(0).getSkuCode(), Matchers.is("iphone_15"));

        System.out.println("Received requests: " + WireMock.getAllServeEvents());

        verify(getRequestedFor(urlPathEqualTo("/api/v1/inventory"))
                .withQueryParam("skuCode", equalTo("iphone_15"))
                .withQueryParam("quantity", equalTo("1")));
    }

    @Test
    void shouldRetrieveExistingOrder() {
        Order order = Order.builder()
                .orderNbr(UUID.randomUUID().toString())
                .skuCode("iphone_15")
                .price(BigDecimal.valueOf(1000))
                .quantity(1)
                .build();
        orderRepository.save(order);

        given()
                .contentType("application/json")
                .when()
                .get("/api/v1/orders/" + order.getOrderNbr())
                .then()
                .log().all()
                .statusCode(200)
                .body("orderNbr", Matchers.is(order.getOrderNbr()))
                .body("skuCode", Matchers.is(order.getSkuCode()))
                .body("price", Matchers.is(1000.0F))
                .body("quantity", Matchers.is(1));
    }

    @Test
    void shouldReturnNotFoundForNonExistingOrder() {
        given()
                .contentType("application/json")
                .when()
                .get("/api/v1/orders/non-existing-nbr")
                .then()
                .log().all()
                .statusCode(404);
    }
}
