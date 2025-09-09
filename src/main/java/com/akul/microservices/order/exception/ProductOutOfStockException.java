package com.akul.microservices.order.exception;

/**
 * ProductOutStockException.java
 *
 * @author Andrii Kulynch
 * @version 1.0
 * @since 9/2/2025
 */
public class ProductOutOfStockException extends RuntimeException {
    public ProductOutOfStockException(String skuCode) {
       super("Order with skuCode " + skuCode + " not found in stock");
    }
}
