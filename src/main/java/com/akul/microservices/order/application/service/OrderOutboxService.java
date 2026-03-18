package com.akul.microservices.order.application.service;

import com.akul.microservices.order.infrastructure.outbox.OrderOutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

/**
 * OutboxService.java.
 *
 * @author Andrii Kulynych
 * @since 3/6/2026
 */
@Service
@RequiredArgsConstructor
public class OrderOutboxService {

    private final OrderOutboxRepository repository;

    @Transactional(propagation = REQUIRES_NEW)
    public void markProcessed(Long id) {
        repository.markProcessed(id);
    }

    @Transactional(propagation = REQUIRES_NEW)
    public void markFailed(Long id) {
        repository.markFailed(id);
    }
}
