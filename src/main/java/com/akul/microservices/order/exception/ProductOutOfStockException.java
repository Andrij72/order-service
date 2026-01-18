package com.akul.microservices.order.exception;

import lombok.Getter;

/**
 * ProductOutStockException.java.
 *
 * @author Andrii Kulynch
 * @version 1.0
 * @since 9/2/2025
 */
@Getter
public class ProductOutOfStockException extends RuntimeException {
    private final String sku;

    public ProductOutOfStockException(String sku) {
        super("Product with SKU '" + sku + "' is out of stock");
        this.sku = sku;
    }

}
