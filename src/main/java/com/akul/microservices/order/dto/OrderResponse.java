package com.akul.microservices.order.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * OrderResponse.java.
 *
 * @author Andrii Kulynch
 * @version 1.0
 * @since 8/23/2025
 */
public record OrderResponse(
        String orderNumber,
        UserDetails userDetails,
        List<OrderItemResponse> items,
        String status,
        Instant createdAt
) {
    public record UserDetails(
            String email,
            String firstName,
            String lastName
    ) {}

    public record OrderItemResponse(
            String sku,
            BigDecimal price,
            Integer quantity
    ) {}
}
