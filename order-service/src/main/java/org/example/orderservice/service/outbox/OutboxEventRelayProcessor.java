package org.example.orderservice.service.outbox;

import jakarta.persistence.EntityNotFoundException;
import java.sql.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.orderservice.domain.outbox.OutboxEvent;
import org.example.orderservice.domain.outbox.OutboxEventRepository;
import org.example.orderservice.kafka.producer.KafkaProducer;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventRelayProcessor {
    private final OutboxEventRepository outboxEventRepository;
    private final KafkaProducer kafkaProducer;
    private final OutboxEventStatusUpdater outboxEventStatusUpdater;

    public void process(String id) {
        OutboxEvent outboxEvent =
                outboxEventRepository.findById(id)
                                     .orElseThrow(() -> new EntityNotFoundException("Outbox event not found"));
        process(outboxEvent);
    }

    private void process(OutboxEvent outboxEvent) {
        log.info("[OutboxEventRelayProcessor] Start processing Outbox Event: {}", outboxEvent);
        kafkaProducer.send(outboxEvent.getTopic(), outboxEvent.getKey(), outboxEvent.getPayload(),
                           result -> handleSuccess(outboxEvent, result),
                           failure -> handleFailure(outboxEvent, failure)
        );
    }

    private void handleSuccess(OutboxEvent outboxEvent, SendResult<String, String> result) {
        log.info("[OutboxEventRelayProcessor] Successfully processed Outbox Event: {}", outboxEvent.getId());
        outboxEventStatusUpdater.updateToSuccess(outboxEvent.getId(), new Timestamp(result.getRecordMetadata().timestamp()).toLocalDateTime());
    }

    private void handleFailure(OutboxEvent outboxEvent, Throwable failure) {
        log.error("[OutboxEventRelayProcessor] Error processing Outbox Event: {}, Error: {}", outboxEvent.getId(), failure.getMessage(), failure);
        outboxEventStatusUpdater.updateToFail(outboxEvent.getId(), failure);
    }

}
