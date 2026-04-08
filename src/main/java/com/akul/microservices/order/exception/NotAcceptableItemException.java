package com.akul.microservices.order.exception;

/**
 * NotAcceptableItemException.java.
 *
 * @author Andrii Kulynych
 * @since 12/17/2025
 */
public class NotAcceptableItemException extends RuntimeException {
    public NotAcceptableItemException() {
        super("Order must contain at least one item");
    }
}
