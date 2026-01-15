package com.akul.microservices.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

/**
 * OrderRequest.java.
 *
 * @author Andrii Kulynch
 * @since 8/22/2025
 */
public record OrderRequest(
        @NotNull
        @NotEmpty
        List<OrderItemRequest> items,
        @NotNull
        UserDetails userDetails
) {
    public record OrderItemRequest(
            @NotBlank
            String sku,
            @NotBlank
            String productName,
            @NotNull
            BigDecimal price,
            @Min(1)
            Integer quantity
    ) {}
    public record UserDetails(
            String email,
            String firstName,
            String lastName
    ) {}
}
