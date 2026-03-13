package com.akul.microservices.order.service;

import com.akul.microservices.order.application.dto.OrderRequest;
import com.akul.microservices.order.application.dto.OrderResponse;
import com.akul.microservices.order.application.dto.PageRequestDto;
import com.akul.microservices.order.application.mappers.OrderEventMapper;
import com.akul.microservices.order.application.mappers.OrderMapper;
import com.akul.microservices.order.domain.exception.BadRequestException;
import com.akul.microservices.order.domain.exception.NotAcceptableItemException;
import com.akul.microservices.order.domain.exception.OrderNotFoundException;
import com.akul.microservices.order.domain.model.Order;
import com.akul.microservices.order.domain.model.OrderStatus;
import com.akul.microservices.order.event.OrderPlacedEvent;
import com.akul.microservices.order.infrastructure.outbox.OrderEventType;
import com.akul.microservices.order.infrastructure.outbox.OrderOutbox;
import com.akul.microservices.order.infrastructure.outbox.OrderOutboxRepository;
import com.akul.microservices.order.infrastructure.persistance.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
    private static final Set<String> SORT_WHITELIST = Set.of(
            "createdAt",
            "updatedAt",
            "status",
            "orderNumber"
    );

    private final OrderRepository orderRepository;
    private final OrderMapper mapper;
    private final OrderOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;


    // ---------------------------------------------------------------------
    // CREATE
    // ---------------------------------------------------------------------
    @Transactional
    public OrderResponse placeOrder(OrderRequest request) {

        if (request.items() == null || request.items().isEmpty()) {
            throw new NotAcceptableItemException();
        }

        Order order = Order.create(
                UUID.randomUUID().toString(),
                mapper.toEntity(request.userDetails())
        );

        mapper.updateItems(order, request.items());
        Order saved = orderRepository.save(order);

        publishEvent(saved, OrderEventType.ORDER_CREATED);

        log.info("Order placed: {}", saved.getOrderNumber());

        return mapper.toResponse(saved);
    }

    private void publishEvent(Order saved, OrderEventType eventType) {

        OrderPlacedEvent event = OrderEventMapper.map(saved);

        byte[] payload;

        try {
            payload = event.toByteBuffer().array();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        OrderOutbox outbox = OrderOutbox.create(
                saved.getOrderNumber(),
                eventType,
                payload
        );

        outboxRepository.save(outbox);
    }

    // ---------------------------------------------------------------------
    // ORDER CANCEL
    // ---------------------------------------------------------------------
    @Transactional
    public OrderResponse cancelOrder(String orderNumber) {

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException(orderNumber));

        order.cancel();

        Order saved = orderRepository.save(order);

        publishEvent(saved, OrderEventType.ORDER_CANCELLED);

        return mapper.toResponse(saved);
    }

    // ---------------------------------------------------------------------
    // UPDATE FULL ORDER
    // ---------------------------------------------------------------------
    @Transactional
    public OrderResponse update(String orderNumber, OrderRequest request) {

        Order existing = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException(orderNumber));
        existing.updateUserDetails(
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
    public Page<OrderResponse> getAllOrders(
            PageRequestDto dto,
            String status,
            String email
    ) {
        Pageable pageable = PageRequest.of(
                dto.pageOrDefault(),
                dto.sizeOrDefault(),
                parseSort(dto.sort()) // беремо sort з DTO
        );

        Page<Order> page;

        if (status != null && email != null) {
            page = orderRepository.findByStatusAndUserDetailsEmail(
                    OrderStatus.valueOf(status),
                    email,
                    pageable
            );
        } else if (status != null) {
            page = orderRepository.findByStatus(
                    OrderStatus.valueOf(status),
                    pageable
            );
        } else if (email != null) {
            page = orderRepository.findByUserDetailsEmail(
                    email,
                    pageable
            );
        } else {
            page = orderRepository.findAll(pageable);
        }

        return page.map(mapper::toResponse);
    }

    // -------------------------------
    // UTIL
    // --------------------------------
    private Sort parseSort(List<String> sortParams) {
        if (sortParams == null || sortParams.isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        List<Sort.Order> orders = new ArrayList<>();

        for (String param : sortParams) {
            if (param == null || param.isBlank()) {
                continue;
            }

            String[] parts = param.split(",");

            String field = parts[0].trim();

            if (!SORT_WHITELIST.contains(field)) {
                throw new BadRequestException(
                        "Sorting by field '" + field + "' is not allowed"
                );
            }

            Sort.Direction direction = Sort.Direction.ASC;

            if (parts.length > 1) {
                try {
                    direction = Sort.Direction.fromString(parts[1].trim());
                } catch (IllegalArgumentException ex) {
                    throw new BadRequestException(
                            "Invalid sort direction '" + parts[1] + "'. Allowed values: ASC, DESC"
                    );
                }
            }

            orders.add(new Sort.Order(direction, field));
        }

        if (orders.isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        return Sort.by(orders);
    }

}
