package com.akul.microservices.order.infrastructure.outbox;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * OrderOutboxRepository.java.
 *
 * @author Andrii Kulynych
 * @since 2/23/2026
 */
@Repository
public interface OrderOutboxRepository extends JpaRepository<OrderOutbox, Long> {

    @Query(value = """
            SELECT *
            FROM order_outbox
            WHERE status = 'PENDING'
            AND processed_at IS NULL
            AND next_retry_at <= NOW()
            ORDER BY created_at
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<OrderOutbox> findBatchForProcessing(@Param("limit") int limit);

    @Modifying
    @Query(value = """
            UPDATE order_outbox
            SET status = 'PROCESSED',
                processed_at = CURRENT_TIMESTAMP
            WHERE id = :id
            AND status = 'PENDING'
            """, nativeQuery = true)
    void markProcessed(@Param("id") Long id);

    @Modifying
    @Query(value = """
            UPDATE order_outbox
            SET status = 'FAILED',
                processed_at = CURRENT_TIMESTAMP,
                next_retry_at = DATE_ADD(NOW(), INTERVAL 30 SECOND)
            WHERE id = :id
            AND status = 'PENDING'
            """, nativeQuery = true)
    void markFailed(@Param("id") Long id);
}
