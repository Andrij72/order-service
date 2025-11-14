package com.akul.microservices.order.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * KafkaTopicConfig.java.
 *
 * @author Andrii Kulynch
 * @version 1.0
 * @since 9/26/2025
 */

@Configuration
public class KafkaTopicConfig {
    @Bean
    public NewTopic orderPlacedTopic() {
        return TopicBuilder.name("order-placed")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic orderUpdatedTopic() {
        return TopicBuilder.name("order-updated")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic orderNotificationTopic() {
        return TopicBuilder.name("order-notification")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
