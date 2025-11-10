package com.akul.microservices.order.service;

import com.akul.microservices.order.client.InventoryRestClient;
import com.akul.microservices.order.model.Order;
import com.akul.microservices.order.model.UserDetails;
import com.akul.microservices.order.repository.OrderRepository;
import com.akul.microservices.order.stubs.InventoryClientStub;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.flyway.enabled=false",
                "spring.cloud.discovery.enabled=false",
                "eureka.client.enabled=false"
        })
class OrderServiceIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private WireMockServer wireMockServer;

    @Autowired
    private InventoryRestClient inventoryClient;

    @LocalServerPort
    private int port;

    @Container
    static KafkaContainer kafka =
            new KafkaContainer(DockerImageName.parse("apache/kafka:3.7.0"));

    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.3.0")
            .withDatabaseName("orderdb")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("schema.sql");


    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @BeforeEach
    void setup() {
        WireMock.configureFor("localhost", wireMockServer.port());
        wireMockServer.stubFor(WireMock.get("/api/v1/inventory")
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("true")));
    }

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        orderRepository.deleteAll();
        wireMockServer.resetAll();
        InventoryClientStub.stubInventoryCall("iphone_15", 1, wireMockServer);
        System.out.println(" WireMockServer running on port: " + wireMockServer.port());
    }

    @Test
    void contextLoads() {
    }

    @Test
    void shouldSubmitOrder() {
        boolean inStock = inventoryClient.isProductInStock("iphone_15", 1);
        assertTrue(inStock, "Product should be in stock");

        WireMock.verify(WireMock.getRequestedFor(WireMock.urlEqualTo("/api/v1/inventory?skuCode=iphone_15&quantity=1")));

        String orderJson = """
                {
                  "skuCode": "iphone_15",
                  "price": 1000.0,
                  "quantity": 1,
                  "userDetails": {
                    "email": "andrii@example.com",
                    "firstName": "Andrii",
                    "lastName": "K"
                  }
                }
                """;

        given()
                .port(port)
                .contentType("application/json")
                .body(orderJson)
                .when()
                .post("/api/v1/orders")
                .then()
                .log().all()
                .statusCode(201)
                .body("skuCode", Matchers.is("iphone_15"))
                .body("userDetails.email", Matchers.is("andrii@example.com"))
                .body("orderNbr", Matchers.notNullValue());

        verify(getRequestedFor(urlPathEqualTo("/api/v1/inventory"))
                .withQueryParam("skuCode", equalTo("iphone_15"))
                .withQueryParam("quantity", equalTo("1")));


        var savedOrders = orderRepository.findAll();
        assertThat(savedOrders).hasSize(1);
        assertThat(savedOrders.get(0).getSkuCode()).isEqualTo("iphone_15");

    }

    @Test
    void shouldRetrieveExistingOrder() {
        Order order = Order.builder()
                .orderNbr(UUID.randomUUID().toString())
                .skuCode("iphone_15")
                .price(BigDecimal.valueOf(1000))
                .quantity(1)
                .userDetails(new UserDetails("andrii@example.com", "Andrii", "K"))
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
                .body("quantity", Matchers.is(1))
                .body("userDetails.email", Matchers.is("andrii@example.com"));
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
