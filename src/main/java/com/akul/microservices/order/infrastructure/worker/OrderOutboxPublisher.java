package com.akul.microservices.order.infrastructure.worker;

import com.akul.microservices.order.event.OrderPlacedEvent;
import com.akul.microservices.order.infrastructure.messaging.kafka.OrderTopicResolver;
import com.akul.microservices.order.infrastructure.outbox.OrderOutbox;
import com.akul.microservices.order.infrastructure.outbox.OrderOutboxRepository;
import com.akul.microservices.order.service.OrderOutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
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

    private final OrderOutboxRepository outboxRepository;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;
    private final OrderTopicResolver topicResolver;
    private final OrderOutboxService outboxService;

    @Scheduled(fixedDelayString = "${outbox.publisher.delay:5000}")
    public void publishEvents() {

        List<OrderOutbox> events =
                outboxRepository.findBatchForProcessing(BATCH_SIZE);

        for (OrderOutbox event : events) {
            publishAsync(event);
        }
    }

    public void publishAsync(OrderOutbox outbox) {

        String topic = topicResolver.resolveTopic(outbox.getEventType());

        try {

            OrderPlacedEvent event =
                    OrderPlacedEvent.fromByteBuffer(
                            ByteBuffer.wrap(outbox.getPayload())
                    );

            kafkaTemplate
                    .send(topic, event.getOrderNbr().toString(), event)
                    .whenComplete((result, ex) -> {

                        if (ex != null) {

                            log.error(
                                    "Kafka publish failed: order={}, topic={}",
                                    event.getOrderNbr(),
                                    topic,
                                    ex
                            );

                            outboxService.markFailed(outbox.getId());
                            return;
                        }

                        log.info(
                                "Kafka ACK topic={}, partition={}, offset={}",
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset()
                        );

                        outboxService.markProcessed(outbox.getId());
                    });

        } catch (Exception ex) {

            log.error(
                    "Outbox deserialize failed id={}",
                    outbox.getId(),
                    ex
            );

            outboxService.markFailed(outbox.getId());
        }
    }
}
