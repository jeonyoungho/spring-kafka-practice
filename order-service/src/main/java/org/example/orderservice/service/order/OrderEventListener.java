package org.example.orderservice.service.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static org.example.orderservice.config.AsyncConfig.OUTBOX_EVENT_TASK_EXECUTOR;
import org.example.orderservice.domain.order.OrderEvent;
import org.example.orderservice.domain.outbox.OutboxEvent;
import org.example.orderservice.service.outbox.OutboxEventRelayProcessor;
import org.example.orderservice.domain.outbox.OutboxEventRepository;
import org.example.orderservice.kafka.util.KafkaJsonConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    @Value("${spring.kafka.topic.order-event}")
    private String orderEventTopic;

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaJsonConverter kafkaJsonConverter;
    private final OutboxEventRelayProcessor outboxEventRelayProcessor;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleBeforeCommit(OrderEvent event) {
        outboxEventRepository.save(OutboxEvent.create(event.getOutboxEventId(),
                                                      orderEventTopic,
                                                      event.getOrderId().toString(),
                                                      kafkaJsonConverter.serialize(event)));
    }

    @Async(value = OUTBOX_EVENT_TASK_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAfterCommit(OrderEvent event) {
        outboxEventRelayProcessor.process(event.getOutboxEventId());
    }

}
