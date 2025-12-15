package com.akul.microservices.order.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * UpdateOrderStatusRequest.java.
 *
 * @author Andrii Kulynych
 * @since 12/14/2025
 */
public record UpdateOrderStatusRequest (
    @NotBlank String status
) {}
