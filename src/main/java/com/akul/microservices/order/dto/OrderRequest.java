package com.akul.microservices.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * OrderRequest.java
 *
 * @author Andrii Kulynch
 * @since 8/22/2025
 */
public record OrderRequest(Long id,
                           String orderNbr,
                           String skuCode,
                           BigDecimal price,
                           Integer quantity,
                           @NotNull UserDetails userDetails
) {
    public record UserDetails(
            @NotBlank String email,
            @NotBlank String firstName,
            @NotBlank String lastName
    ) {}
}
