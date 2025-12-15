package com.akul.microservices.order.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * OrderRequest.java.
 *
 * @author Andrii Kulynch
 * @since 8/22/2025
 */
public record OrderRequest(
        List<OrderItemRequest> items,
        UserDetails userDetails
) {
    public record OrderItemRequest(
            String sku,
            BigDecimal price,
            Integer quantity
    ) {}
    public record UserDetails(
            String email,
            String firstName,
            String lastName
    ) {}
}
