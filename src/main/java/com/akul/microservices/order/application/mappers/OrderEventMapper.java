package com.akul.microservices.order.application.mappers;
import com.akul.microservices.order.domain.model.Order;
import com.akul.microservices.order.event.OrderItem;
import com.akul.microservices.order.event.OrderPlacedEvent;
import com.akul.microservices.order.event.OrderStatus;
import com.akul.microservices.order.infrastructure.outbox.OrderEventType;

import java.time.Instant;
import java.util.List;

public class OrderEventMapper {

    public static OrderPlacedEvent map(Order order) {

        List<OrderItem> items =
                order.getItems().stream()
                        .map(i -> OrderItem.newBuilder()
                                .setSku(i.getSku())
                                .setPrice(i.getPrice().toPlainString())
                                .setQuantity(i.getQuantity())
                                .build())
                        .toList();

        return OrderPlacedEvent.newBuilder()
                .setOrderNbr(order.getOrderNumber())
                .setEmail(order.getUserDetails().getEmail())
                .setFirstName(order.getUserDetails().getFirstName())
                .setLastName(order.getUserDetails().getLastName())
                .setStatus(OrderStatus.valueOf(order.getStatus().name()))
                .setCreatedAt(Instant.now())
                .setItems(items)
                .build();
    }

    public static OrderStatus toAvroStatus(OrderEventType eventType) {
        return switch (eventType) {
            case ORDER_CREATED -> OrderStatus.PENDING;
            case PAYMENT_REQUESTED -> OrderStatus.WAITING_PAYMENT;
            case ORDER_COMPLETED -> OrderStatus.COMPLETED;
            case ORDER_FAILED -> OrderStatus.FAILED;
            case ORDER_CANCELLED -> OrderStatus.CANCELLED;
        };
    }

    public static OrderStatus toAvroStatus(com.akul.microservices.order.domain.model.OrderStatus status) {
        return switch (status) {
            case PENDING -> OrderStatus.PENDING;
            case WAITING_PAYMENT -> OrderStatus.WAITING_PAYMENT;
            case PAID -> OrderStatus.PAID;
            case DELIVERING -> OrderStatus.DELIVERING;
            case COMPLETED -> OrderStatus.COMPLETED;
            case CANCELLED -> OrderStatus.CANCELLED;
            case FAILED -> OrderStatus.FAILED;
        };
    }
}