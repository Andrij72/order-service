package com.akul.microservices.order.service;

import com.akul.microservices.order.client.InventoryRestClient;
import com.akul.microservices.order.dto.OrderRequest;
import com.akul.microservices.order.dto.OrderResponse;
import com.akul.microservices.order.event.OrderPlacedEvent;
import com.akul.microservices.order.exception.OrderNotFoundException;
import com.akul.microservices.order.exception.ProductOutOfStockException;
import com.akul.microservices.order.mappers.OrderMapper;
import com.akul.microservices.order.model.Order;
import com.akul.microservices.order.model.UserDetails;
import com.akul.microservices.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * OrderService.java
 *
 * @author Andrii Kulynch
 * @since 8/22/2025
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final InventoryRestClient inventoryClient;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    @Transactional
    public OrderResponse placeOrder(OrderRequest orderRequest) {

        boolean isInStock = inventoryClient.isProductInStock(orderRequest.skuCode(), orderRequest.quantity());

        if (!isInStock) {
            throw new ProductOutOfStockException(orderRequest.skuCode());
        }

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

        kafkaTemplate.send("order-placed", event);
        log.info("Sending event to Kafka: {}", event);

        return orderMapper.toDto(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(String orderId) {
        Order order = orderRepository.findByOrderNbr(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        log.info("Order retrieved: {}", orderId);

        return orderMapper.toDto(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        if (orders.isEmpty()) {
            throw new OrderNotFoundException("No orders found");
        }

        return orderMapper.toDto(orders);
    }

    @Transactional
    public void deleteOrder(String orderId) {
        Order order = orderRepository.findByOrderNbr(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        orderRepository.delete(order);
        log.info("Order deleted: {}", orderId);
    }

    @Transactional
    public OrderResponse update(String orderId, OrderRequest orderRequest) {

        Order updatedOrder = orderRepository.findByOrderNbr(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        updatedOrder.setQuantity(orderRequest.quantity());
        updatedOrder.setPrice(orderRequest.price());
        updatedOrder.setUserDetails(new UserDetails(
                        orderRequest.userDetails().email(),
                        orderRequest.userDetails().firstName(),
                        orderRequest.userDetails().lastName()
                )
        );

        orderRepository.save(updatedOrder);
        log.info("Order updated: {}", orderId);

        return orderMapper.toDto(updatedOrder);
    }
}
