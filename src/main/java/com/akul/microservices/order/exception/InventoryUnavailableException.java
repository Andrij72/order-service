package com.akul.microservices.order.exception;

/**
 * InventoryUnavailableException.java.
 *
 * @author Andrii Kulynych
 * @since 11/1/2025
 */
public class InventoryUnavailableException extends RuntimeException {
    public InventoryUnavailableException(String s) {
        super(s);
    }
}
