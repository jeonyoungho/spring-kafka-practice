package org.example.orderservice.service.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.orderservice.domain.order.OrderEvent;
import org.example.orderservice.domain.outbox.OutboxEvent;
import org.example.orderservice.domain.outbox.OutboxEventRepository;
import org.example.orderservice.kafka.util.KafkaJsonConverter;
import org.springframework.beans.factory.annotation.Value;
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

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void listen(OrderEvent event) {
        outboxEventRepository.save(OutboxEvent.create(orderEventTopic,
                                                      event.getOrderId().toString(),
                                                      kafkaJsonConverter.serialize(event)));
    }

}
