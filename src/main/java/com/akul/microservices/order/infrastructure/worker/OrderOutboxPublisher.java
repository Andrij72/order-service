package com.akul.microservices.order.infrastructure.worker;

import com.akul.microservices.order.infrastructure.outbox.OrderOutbox;
import com.akul.microservices.order.infrastructure.outbox.OrderOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;

import java.util.List;

/**
 * OrderOutboxPublisher.java.
 *
 * @author Andrii Kulynych
 * @since 2/23/2026
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderOutboxPublisher {

    private static final int BATCH_SIZE = 100;
    private static final String TOPIC = "order-events";
    private final OrderOutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Transactional
    @Scheduled(fixedDelayString = "${outbox.publisher.delay:5000}")
    public void publishEvents() {

        List<OrderOutbox> events =
                outboxRepository.findBatchForUpdate(
                        PageRequest.of(0, BATCH_SIZE)
                );

        for (OrderOutbox event : events) {

            try {

                kafkaTemplate.send(
                        TOPIC,
                        event.getPayload()
                ).get();

                event.markProcessed();

                outboxRepository.save(event);

            } catch (Exception ex) {
                log.error("Outbox publish failed", ex);
            }
        }
    }
}
