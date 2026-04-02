package com.akul.microservices.order.infrastructure.messaging.kafka;

import com.akul.microservices.inventory.event.InventoryEvent;
import com.akul.microservices.order.application.service.OrderService;
import com.akul.microservices.order.domain.model.Order;
import com.akul.microservices.order.domain.model.OrderStatus;
import com.akul.microservices.order.infrastructure.persistence.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * OrderKafkaListener.java.
 *
 * @author Andrii Kulynych
 * @since 2/23/2026
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderKafkaListener {

    private final OrderService orderService;

    @KafkaListener(topics = "inventory-confirmed", groupId = "order-group")
    public void handleInventoryConfirmed(InventoryEvent event) {
        orderService.handleInventoryConfirmed(event);
    }

    @KafkaListener(topics = "inventory-rejected", groupId = "order-group")
    public void handleInventoryRejected(InventoryEvent event) {
        orderService.handleInventoryRejected(event);
    }

    @KafkaListener(topics = "payment-completed", groupId = "order-group")
    public void handlePaymentCompleted(String orderNumber) {
        orderService.handlePaymentCompleted(orderNumber);
    }

    @KafkaListener(topics = "payment-failed", groupId = "order-group")
    public void handlePaymentFailed(String orderNumber) {
        orderService.handlePaymentFailed(orderNumber);
    }
}
