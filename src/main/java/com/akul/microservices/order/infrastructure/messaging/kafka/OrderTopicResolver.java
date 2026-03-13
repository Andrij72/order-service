package com.akul.microservices.order.infrastructure.messaging.kafka;

import com.akul.microservices.order.infrastructure.outbox.OrderEventType;
import org.springframework.stereotype.Component;

/**
 * OrderTopicResolver.java.
 *
 * @author Andrii Kulynych
 * @since 3/1/2026
 */
@Component
public class OrderTopicResolver {

    public String resolveTopic(OrderEventType type) {

        return switch (type) {
            case ORDER_CREATED -> "order-created";
            case ORDER_FAILED -> "order-failed";
            case ORDER_CANCELLED -> "order-cancelled";
            case ORDER_COMPLETED-> "order-completed";
        };
    }
}