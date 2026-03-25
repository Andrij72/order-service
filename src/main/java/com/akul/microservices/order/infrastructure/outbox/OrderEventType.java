package com.akul.microservices.order.infrastructure.outbox;

/**
 * OrderEventType.java.
 *
 * @author Andrii Kulynych
 * @since 2/28/2026
 */
public enum OrderEventType {
    ORDER_CREATED,
    ORDER_FAILED,
    ORDER_CANCELLED,
    PAYMENT_REQUESTED,
    ORDER_COMPLETED
    }
