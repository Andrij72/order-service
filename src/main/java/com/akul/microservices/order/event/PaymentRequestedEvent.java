package com.akul.microservices.order.event;

/**
 * PaymentRequestedEvent.java.
 *
 * @author Andrii Kulynych
 * @since 2/23/2026
 */
public record PaymentRequestedEvent(
        String orderNumber,
        Double amount
) {}
