package com.akul.microservices.order.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

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
        @JsonFormat(shape = JsonFormat.Shape.STRING,
                pattern = "yyyy-MM-dd HH:mm:ss",
                timezone = "Europe/Berlin")
        Instant createdAt
) {
    public record UserDetails(
            String email,
            String firstName,
            String lastName
    ) {
    }

    public record OrderItemResponse(
            String sku,
            String name,
            BigDecimal price,
            Integer quantity
    ) {
    }
}
