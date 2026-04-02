package com.akul.microservices.order.infrastructure.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String aggregateId;

    @Enumerated(EnumType.STRING)
    private OrderEventType eventType;

    @Column(name = "payload", columnDefinition = "MEDIUMBLOB",
            nullable = false)
    private byte[] payload;

    @Enumerated(EnumType.STRING)
    private Status status;

    private int retryCount;

    private Instant nextRetryAt;

    private Instant createdAt;

    private Instant processedAt;

    @Version
    private Long version;

    public enum Status {
        PENDING,
        PROCESSING,
        PROCESSED,
        FAILED
    }

    public static OrderOutbox create(
            String aggregateId,
            OrderEventType eventType,
            byte[] payload
    ) {
        OrderOutbox outbox = new OrderOutbox();

        outbox.aggregateId = aggregateId;
        outbox.eventType = eventType;
        outbox.payload = payload;

        outbox.status = Status.PENDING;
        outbox.retryCount = 0;
        outbox.createdAt = Instant.now();
        outbox.nextRetryAt = Instant.now();

        return outbox;
    }

    private void validatePersisted() {
        if (id == null) {
            throw new IllegalStateException("Entity is not persisted");
        }
    }

    private void validateMutable() {
        if (status == Status.PROCESSED ||
            status == Status.FAILED) {

            throw new IllegalStateException("Terminal state");
        }
    }

    public void markProcessing() {
        validatePersisted();
        validateMutable();
        this.status = Status.PROCESSING;
    }

    public void markProcessed() {
        validatePersisted();
        validateMutable();
        this.status = Status.PROCESSED;
        this.processedAt = Instant.now();
    }

    public void markFailed() {

        validatePersisted();

        retryCount++;

        if (retryCount > 5) {
            this.status = Status.FAILED;
            return;
        }

        this.status = Status.PENDING;

        long backoff = Math.min(
                30L * (1L << retryCount),
                300
        );

        this.nextRetryAt = Instant.now().plusSeconds(backoff);
    }
}
