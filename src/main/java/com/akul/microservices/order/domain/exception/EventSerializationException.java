package com.akul.microservices.order.domain.exception;

/**
 * ventSerializationException.java.
 *
 * @author Andrii Kulynych
 * @since 3/3/2026
 */
public class EventSerializationException extends RuntimeException{
    public EventSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
