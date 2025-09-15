package com.akul.microservices.order.service;

import com.akul.microservices.order.client.InventoryClient;
import com.akul.microservices.order.dto.OrderRequest;
import com.akul.microservices.order.dto.OrderResponse;
import com.akul.microservices.order.event.OrderPlacedEvent;
import com.akul.microservices.order.exception.OrderNotFoundException;
import com.akul.microservices.order.exception.ProductOutOfStockException;
import com.akul.microservices.order.model.Order;
import com.akul.microservices.order.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
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
@Transactional
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private  final KafkaTemplate<String,OrderPlacedEvent> kafkaTemplate;

    public OrderResponse placeOrder(OrderRequest orderRequest) {
        var isInStock = inventoryClient.isProductInStock(orderRequest.skuCode(), orderRequest.quantity());

        if (isInStock) {
            Order order = new Order();
            order.setOrderNbr(UUID.randomUUID().toString());
            order.setPrice(orderRequest.price());
            order.setSkuCode(orderRequest.skuCode());
            order.setQuantity(orderRequest.quantity());

            Order savedOrder = orderRepository.save(order);

            log.info("New order placed: {}", savedOrder.getOrderNbr());

            OrderPlacedEvent orderPlacedEvent = new OrderPlacedEvent();

            orderPlacedEvent.setOrderNbr(order.getOrderNbr());
            orderPlacedEvent.setEmail(orderRequest.userDetails().email());
            orderPlacedEvent.setFirstName(orderRequest.userDetails().firstName());
            orderPlacedEvent.setLastName(orderRequest.userDetails().lastName());
      log.info("Start. Sending OrderPlacedEvent {} to Kafka topic order-placed", order.getOrderNbr());
            kafkaTemplate.send("order-placed", orderPlacedEvent);
            log.info("End. Sending OrderPlacedEvent {} to Kafka topic order-placed", order.getOrderNbr());

            return new OrderResponse(
                    savedOrder.getOrderNbr(),
                    savedOrder.getSkuCode(),
                    savedOrder.getPrice(),
                    savedOrder.getQuantity()
            );
        } else {
            throw new ProductOutOfStockException(orderRequest.skuCode());
        }

    }

    public OrderResponse getOrder(String orderNbr) {
        Order order = orderRepository.findByOrderNbr(orderNbr)
                .orElseThrow(() -> new OrderNotFoundException(orderNbr));

        log.info("Order retrieved: {}", orderNbr);

        return new OrderResponse(
                order.getOrderNbr(),
                order.getSkuCode(),
                order.getPrice(),
                order.getQuantity()
        );
    }

    public List<OrderResponse> getAllOrders() {
        List<Order> orders = orderRepository.findAll();

        return orders.stream()
                .map(OrderResponse::from)
                .toList();
    }
}