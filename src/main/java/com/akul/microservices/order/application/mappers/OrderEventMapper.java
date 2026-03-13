package com.akul.microservices.order.application.mappers;
import com.akul.microservices.order.domain.model.Order;
import com.akul.microservices.order.event.OrderItem;
import com.akul.microservices.order.event.OrderPlacedEvent;
import com.akul.microservices.order.event.OrderStatus;

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
}