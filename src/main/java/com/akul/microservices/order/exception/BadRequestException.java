package com.akul.microservices.order.exception;

/**
 * BadRequestException.java.
 *
 * @author Andrii Kulynych
 * @since 1/15/2026
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }

}
