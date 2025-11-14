package com.akul.microservices.order.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * OrderRequest.java.
 *
 * @author Andrii Kulynch
 * @since 8/22/2025
 */
public record OrderRequest(
        String skuCode,
        BigDecimal price,
        Integer quantity,
        @NotNull UserDetails userDetails
) {
    public record UserDetails(
            String email,
            String firstName,
            String lastName
    ) {
    }
}
