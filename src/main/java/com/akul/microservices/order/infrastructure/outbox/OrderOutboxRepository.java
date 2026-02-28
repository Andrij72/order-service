package com.akul.microservices.order.infrastructure.outbox;


import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
            AND next_retry_at <= NOW()
            ORDER BY created_at
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<OrderOutbox> findBatchForProcessing(@Param("limit") int limit);
}
