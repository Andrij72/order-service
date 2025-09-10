package com.akul.microservices.order.dto;

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
                           Integer quantity) {
}
