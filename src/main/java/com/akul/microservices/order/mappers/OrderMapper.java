package com.akul.microservices.order.mappers;

import com.akul.microservices.order.dto.OrderRequest;
import com.akul.microservices.order.dto.OrderResponse;
import com.akul.microservices.order.model.Order;
import com.akul.microservices.order.model.OrderItem;
import com.akul.microservices.order.model.OrderStatus;
import com.akul.microservices.order.model.UserDetails;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderNumber", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "items", ignore = true)
    Order toEntity(OrderRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    OrderItem toEntity(OrderRequest.OrderItemRequest request);

    UserDetails toEntity(OrderRequest.UserDetails dto);


    @Mapping(source = "status", target = "status")
    OrderResponse toResponse(Order order);

    OrderResponse.OrderItemResponse toResponse(OrderItem item);

    OrderResponse.UserDetails toResponse(UserDetails userDetails);


    default void updateItems(@MappingTarget Order order,
                             List<OrderRequest.OrderItemRequest> items) {

        order.getItems().clear();

        items.stream()
                .map(this::toEntity)
                .forEach(item -> {
                    item.setOrder(order);
                    order.getItems().add(item);
                });
    }

    default void updateStatus(@MappingTarget Order order, String status) {
        order.setStatus(OrderStatus.valueOf(status));
    }
}
