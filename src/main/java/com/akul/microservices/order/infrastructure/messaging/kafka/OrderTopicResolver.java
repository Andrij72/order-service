package com.akul.microservices.order.infrastructure.messaging.kafka;

import com.akul.microservices.order.infrastructure.outbox.OrderEventType;
import org.springframework.stereotype.Component;

@Component
public class OrderTopicResolver {

    public String resolveTopic(OrderEventType eventType) {

        return switch (eventType) {
            case ORDER_CREATED -> "order-created";
            case PAYMENT_REQUESTED -> "payment-requested";
            case ORDER_COMPLETED -> "order-completed";
            case ORDER_FAILED -> "order-failed";
            case ORDER_CANCELLED -> "order-cancelled";
        };
    }
}
