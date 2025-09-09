package com.akul.microservices.order.dto;

import com.akul.microservices.order.model.Order;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * OrderResponse.java
 *
 * @author Andrii Kulynch
 * @version 1.0
 * @since 8/23/2025
 */
public record OrderResponse(String orderNbr,
                            String skuCode,
                            BigDecimal price,
                            Integer quantity
) {
public static OrderResponse from(Order order){
    return new OrderResponse(order.getOrderNbr(), order.getSkuCode(), order.getPrice(), order.getQuantity());
}

}