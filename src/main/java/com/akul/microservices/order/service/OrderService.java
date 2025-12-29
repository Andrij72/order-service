package com.akul.microservices.order.service;

import com.akul.microservices.order.client.InventoryRestClient;
import com.akul.microservices.order.dto.OrderRequest;
import com.akul.microservices.order.dto.OrderResponse;
import com.akul.microservices.order.event.OrderPlacedEvent;
import com.akul.microservices.order.exception.NotAcceptableItemException;
import com.akul.microservices.order.exception.OrderNotFoundException;
import com.akul.microservices.order.exception.ProductOutOfStockException;
import com.akul.microservices.order.mappers.OrderEventMapper;
import com.akul.microservices.order.mappers.OrderMapper;
import com.akul.microservices.order.model.Order;
import com.akul.microservices.order.model.OrderStatus;
import com.akul.microservices.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


/**
 * OrderService.java.
 *
 * @author Andrii Kulynch
 * @since 8/22/2025
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper mapper;
    private final InventoryRestClient inventoryClient;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    // ---------------------------------------------------------------------
    // CREATE
    // ---------------------------------------------------------------------
    @Transactional
    public OrderResponse placeOrder(OrderRequest request){

        if (request.items() == null || request.items().isEmpty()) {
            throw new NotAcceptableItemException();
        }

        request.items().forEach(item -> {
            boolean inStock =
                    inventoryClient.isProductInStock(item.sku(), item.quantity());
            if (!inStock) {
                throw new ProductOutOfStockException(item.sku());
            }
        });

        Order order = mapper.toEntity(request);

        mapper.updateItems(order, request.items());

        order.setOrderNumber(UUID.randomUUID().toString());
        order.setStatus(OrderStatus.PENDING);

        order.getItems().forEach(i -> i.setOrder(order));

        Order saved = orderRepository.save(order);

        OrderPlacedEvent event = OrderEventMapper.map(saved);
        kafkaTemplate.send("order-placed", event);

        log.info("Order placed: {}", saved.getOrderNumber());
        return mapper.toResponse(saved);
    }

    // ---------------------------------------------------------------------
    // UPDATE STATUS
    // ---------------------------------------------------------------------
    @Transactional
    public OrderResponse updateStatus(String orderNumber, String status) {

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException(orderNumber));

        mapper.updateStatus(order, status);

        Order saved = orderRepository.save(order);

        kafkaTemplate.send(
                "order-status-updated",
                OrderEventMapper.map(saved)
        );

        log.info("Order status updated: {}", saved.getOrderNumber());
        return mapper.toResponse(saved);
    }

    // ---------------------------------------------------------------------
    // UPDATE FULL ORDER
    // ---------------------------------------------------------------------
    @Transactional
    public OrderResponse update(String orderNumber, OrderRequest request) {

        Order existing = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException(orderNumber));
        existing.setUserDetails(
                mapper.toEntity(request.userDetails())
        );

        mapper.updateItems(existing, request.items());

        Order saved = orderRepository.save(existing);
        log.info("Order updated: {}", saved.getOrderNumber());

        return mapper.toResponse(saved);
    }

    // ---------------------------------------------------------------------
    // GET ONE
    // ---------------------------------------------------------------------
    @Transactional(readOnly = true)
    public OrderResponse getOrder(String orderNumber) {

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException(orderNumber));

        return mapper.toResponse(order);
    }

    // ---------------------------------------------------------------------
    // GET ALL
    // ---------------------------------------------------------------------
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {

        return orderRepository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    // ---------------------------------------------------------------------
    // DELETE
    // ---------------------------------------------------------------------
    @Transactional
    public void deleteOrder(String orderNumber) {

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException(orderNumber));

        orderRepository.delete(order);

        log.info("Order deleted: {}", orderNumber);
    }
}
