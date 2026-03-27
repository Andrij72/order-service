package com.akul.microservices.order.service;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class KafkaTestConfig {

    @Bean
    @SuppressWarnings("unchecked")
    public <T> KafkaTemplate<String, T> kafkaTemplate() {
        return mock(KafkaTemplate.class);
    }
}
