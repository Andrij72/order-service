package com.akul.microservices.order.domain.exception;

/**
 * OrderNotFoundException.java.
 *
 * @author Andrii Kulynch
 * @version 1.0
 * @since 8/23/2025
 */
public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String orderNmr) {
        super("Order " +  orderNmr + " not found!");
    }
}
