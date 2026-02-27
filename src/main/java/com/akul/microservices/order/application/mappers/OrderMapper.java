package com.akul.microservices.order.application.mappers;

import com.akul.microservices.order.application.dto.OrderRequest;
import com.akul.microservices.order.application.dto.OrderResponse;
import com.akul.microservices.order.domain.model.Order;
import com.akul.microservices.order.domain.model.OrderItem;
import com.akul.microservices.order.domain.model.OrderStatus;
import com.akul.microservices.order.domain.model.UserDetails;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

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

    @Mapping(target = "items", source = "items")
    OrderResponse toResponse(Order order);

    @Mapping(source = "productName", target = "name")
    OrderResponse.OrderItemResponse toResponse(OrderItem item);

    default String map(OrderStatus status) {
        return status != null ? status.name() : null;
    }

    default void updateItems(@MappingTarget Order order,
                             List<OrderRequest.OrderItemRequest> items) {

        if (items == null || items.isEmpty()) {
            return;
        }

        order.getItems().clear();

        items.stream()
                .map(this::toEntity)
                .forEach(item -> {
                    item.setOrder(order);
                    order.getItems().add(item);
                });
    }
  }
