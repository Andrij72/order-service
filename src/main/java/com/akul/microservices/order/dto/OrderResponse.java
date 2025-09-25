package com.akul.microservices.order.dto;

import com.akul.microservices.order.model.Order;
import com.akul.microservices.order.model.UserDetails;

import java.math.BigDecimal;

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
                            Integer quantity,
                            UserDetails userDetails
) {
    public record UserDetails(
            String email,
            String firstName,
            String lastName
    ) {}
}