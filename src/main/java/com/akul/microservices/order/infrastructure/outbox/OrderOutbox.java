package com.akul.microservices.order.infrastructure.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * OrderOutbox.java.
 *
 * @author Andrii Kulynych
 * @since 2/23/2026
 */
@Entity
@Table(name = "order_outbox")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String aggregateId;

    private String eventType;

    @Column(columnDefinition = "json")
    private String payload;

    private boolean processed;

    private int retryCount;

    private Instant nextRetryAt;

    private Instant createdAt;

    public static OrderOutbox create(
            String orderNumber,
            String eventType,
            String payload
    ) {
        OrderOutbox outbox = new OrderOutbox();
        outbox.aggregateId = orderNumber;
        outbox.eventType = eventType;
        outbox.payload = payload;
        outbox.processed = false;
        outbox.retryCount = 0;
        outbox.createdAt = Instant.now();
        outbox.nextRetryAt = Instant.now();
        return outbox;
    }

    public void markProcessed() {
        this.processed = true;
    }

    public void incrementRetry() {
        this.retryCount++;
        this.nextRetryAt = Instant.now().plusSeconds(30);
    }
}
