package com.akul.microservices.order.mappers;

import com.akul.microservices.order.dto.OrderRequest;
import com.akul.microservices.order.dto.OrderResponse;
import com.akul.microservices.order.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * OrderMaper.java.
 *
 * @author Andrii Kulynch
 * @version 1.0
 * @since 9/26/2025
 */
@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(target = "orderNbr", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "userDetails.email", target = "userDetails.email")
    @Mapping(source = "userDetails.firstName", target = "userDetails.firstName")
    @Mapping(source = "userDetails.lastName", target = "userDetails.lastName")
    Order toEntity(OrderRequest orderDto);

    @Mapping(source = "userDetails.email", target = "userDetails.email")
    @Mapping(source = "userDetails.firstName", target = "userDetails.firstName")
    @Mapping(source = "userDetails.lastName", target = "userDetails.lastName")
    OrderResponse toDto(Order order);

    List<OrderResponse> toDto(List<Order> orders);


}
