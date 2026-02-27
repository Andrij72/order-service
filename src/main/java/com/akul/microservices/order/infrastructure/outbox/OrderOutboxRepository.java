package com.akul.microservices.order.infrastructure.outbox;


import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
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

    List<OrderOutbox> findByProcessedFalse();

    List<OrderOutbox> findByProcessedFalseOrderByCreatedAtAsc(Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    select o from OrderOutbox o
    where o.processed = false
    order by o.createdAt asc
""")
    List<OrderOutbox> findBatchForUpdate(Pageable pageable);
}
