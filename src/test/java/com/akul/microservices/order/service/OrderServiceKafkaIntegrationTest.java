package com.akul.microservices.order.service;

import com.akul.microservices.order.dto.OrderRequest;
import com.akul.microservices.order.dto.OrderResponse;
import com.akul.microservices.order.event.OrderPlacedEvent;
import com.akul.microservices.order.repository.OrderRepository;
import kafka.server.ReplicaManager;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Testcontainers
class OrderServiceKafkaIntegrationTest {

//    @Container
//    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("apache/kafka:3.7.0"));
//
//
//    @DynamicPropertySource
//    static void kafkaProperties(DynamicPropertyRegistry registry) {
//        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
//    }
//
//    @Autowired
//    private KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;
//
//    @Autowired
//    private OrderService orderService;
//
//    private Consumer<String, OrderPlacedEvent> consumer;
//    private String topicName;
//
//    @BeforeEach
//    void setUp() {
//        topicName = "order-placed";
//        JsonDeserializer<OrderPlacedEvent> deserializer = new JsonDeserializer<>(OrderPlacedEvent.class);
//        deserializer.addTrustedPackages("*");
//        deserializer.setUseTypeMapperForKey(false);
//
//        Map<String, Object> consumerProps = Map.of(
//                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
//                ConsumerConfig.GROUP_ID_CONFIG, "testGroup-" + UUID.randomUUID(),
//                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"
//        );
//
//        consumer = new DefaultKafkaConsumerFactory<>(
//                consumerProps,
//                new StringDeserializer(),
//                deserializer
//        ).createConsumer();
//
//        consumer.subscribe(List.of(topicName));
//    }
//
//
//    @AfterEach
//    void tearDown() {
//        if (consumer != null) consumer.close();
//    }
//
//    @Test
//    void shouldPlaceOrderAndSendEventToKafka() throws InterruptedException {
//        OrderRequest request = new OrderRequest(
//                "iphone_15",
//                BigDecimal.valueOf(1000),
//                1,
//                new OrderRequest.UserDetails("andrii@example.com", "Andrii", "K")
//        );
//        OrderResponse response = orderService.placeOrder(request);
//
//        kafkaTemplate.flush();
//        Thread.sleep(500);
//        ConsumerRecord<String, OrderPlacedEvent> record =
//                KafkaTestUtils.getSingleRecord(consumer, topicName, Duration.ofSeconds(5));
//
//        OrderPlacedEvent event = record.value();
//
//        assertEquals(response.orderNbr(), event.getOrderNbr());
//        assertEquals(request.userDetails().email(), event.getEmail());
//        assertEquals(request.userDetails().firstName(), event.getFirstName());
//        assertEquals(request.userDetails().lastName(), event.getLastName());
//    }
}
