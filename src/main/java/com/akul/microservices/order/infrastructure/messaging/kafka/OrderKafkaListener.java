package com.akul.microservices.order.infrastructure.messaging.kafka;

import com.akul.microservices.inventory.event.InventoryEvent;
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

    private final OrderRepository orderRepository;

    @KafkaListener(topics = "inventory-confirmed", groupId = "order-group")
    @Transactional
    public void handleInventoryConfirmed(InventoryEvent event) {

        String orderNumber = event.getOrderNbr();
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow();

        if (!order.getStatus().canTransitionTo(OrderStatus.WAITING_PAYMENT)) {
            return;
        }

        order.markWaitingPayment();
        orderRepository.save(order);

        log.info("Order {} moved to WAITING_PAYMENT", orderNumber);
    }

    @KafkaListener(topics = "inventory-rejected", groupId = "order-group")
    @Transactional
    public void handleInventoryRejected(InventoryEvent event) {

        String orderNumber = event.getOrderNbr();
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow();

        order.markCancelled();
        orderRepository.save(order);

        log.info("Order {} cancelled", orderNumber);
    }

    @KafkaListener(topics = "payment-completed", groupId = "order-group")
    @Transactional
    public void handlePaymentCompleted(String orderNumber) {

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow();

        if (!order.getStatus().canTransitionTo(OrderStatus.PAID)) return;

        order.markPaid();
        orderRepository.save(order);

        log.info("Order {} paid", orderNumber);
    }

    @KafkaListener(topics = "payment-failed", groupId = "order-group")
    @Transactional
    public void handlePaymentFailed(String orderNumber) {

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow();

        order.markFailed();
        orderRepository.save(order);

        log.info("Order {} failed payment", orderNumber);
    }
}

