package com.akul.microservices.order.mappers;
import com.akul.microservices.order.event.OrderItem;
import com.akul.microservices.order.model.Order;
import com.akul.microservices.order.event.OrderPlacedEvent;

import java.util.List;

public class OrderEventMapper {

    public static OrderPlacedEvent map(Order order) {

        List<OrderItem> items =
                order.getItems().stream()
                        .map(i -> OrderItem.newBuilder()
                                .setSku(i.getSku())
                                .setPrice(i.getPrice().doubleValue())
                                .setQuantity(i.getQuantity())
                                .build())
                        .toList();

        return OrderPlacedEvent.newBuilder()
                .setOrderNbr(order.getOrderNumber())
                .setEmail(order.getUserDetails().getEmail())
                .setFirstName(order.getUserDetails().getFirstName())
                .setLastName(order.getUserDetails().getLastName())
                .setStatus(order.getStatus().name())
                .setItems(items)
                .build();
    }
}