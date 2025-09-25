package com.akul.microservices.order.service;

import com.akul.microservices.order.client.InventoryClient;
import com.akul.microservices.order.dto.OrderRequest;
import com.akul.microservices.order.dto.OrderResponse;
import com.akul.microservices.order.event.OrderPlacedEvent;
import com.akul.microservices.order.exception.OrderNotFoundException;
import com.akul.microservices.order.exception.ProductOutOfStockException;
import com.akul.microservices.order.mappers.OrderMapper;
import com.akul.microservices.order.model.Order;
import com.akul.microservices.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * OrderService.java
 *
 * @author Andrii Kulynch
 * @version 1.0
 * @since 8/22/2025
 */

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final InventoryClient inventoryClient;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    @Transactional
    public OrderResponse placeOrder(OrderRequest orderRequest) {
        var isInStock = inventoryClient.isProductInStock(orderRequest.skuCode(), orderRequest.quantity());

        if (isInStock) {
            Order order = orderMapper.toEntity(orderRequest);
            if (order.getOrderNbr() == null) {
                order.setOrderNbr(UUID.randomUUID().toString());
            }

            Order savedOrder = orderRepository.save(order);

            log.info("New order placed: {}", savedOrder.getOrderNbr());

            OrderPlacedEvent event = new OrderPlacedEvent(
                    order.getOrderNbr().toString(),
                    order.getUserDetails().getEmail(),
                    order.getUserDetails().getFirstName(),
                    order.getUserDetails().getLastName()
            );
            log.info("Start. Sending OrderPlacedEvent {} to Kafka", order.getOrderNbr());
            kafkaTemplate.send("order-placed", event);

            log.info("End. Sending OrderPlacedEvent {} to Kafka", order.getOrderNbr());
            log.info("Sending event to Kafka: {}", event);

            return orderMapper.toDto(order);

        } else {
            throw new ProductOutOfStockException(orderRequest.skuCode());
        }

    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(String orderNbr) {
        Order order = orderRepository.findByOrderNbr(orderNbr)
                .orElseThrow(() -> new OrderNotFoundException(orderNbr));

        log.info("Order retrieved: {}", orderNbr);

        return orderMapper.toDto(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        List<Order> orders = orderRepository.findAll();

        return orderMapper.toDto(orders);
    }
}