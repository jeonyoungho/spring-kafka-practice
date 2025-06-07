package org.example.orderservice.service.outbox;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.orderservice.domain.outbox.OutboxEvent;
import org.example.orderservice.domain.outbox.OutboxEvent.Status;
import org.example.orderservice.domain.outbox.OutboxEventRepository;
import org.example.orderservice.domain.outbox.OutboxEventStatusUpdater;
import org.example.orderservice.domain.outbox.OutboxEventStatusUpdater.OutboxEventFailStatusUpdateCommand;
import org.example.orderservice.domain.outbox.OutboxEventStatusUpdater.OutboxEventSuccessStatusUpdateCommand;
import org.example.orderservice.kafka.producer.KafkaProducer;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventRelayProcessor {

    private static final int BATCH_SIZE = 100;

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaProducer kafkaProducer;
    private final OutboxEventStatusUpdater outboxEventStatusUpdater;

    @Scheduled(initialDelay = 5000, fixedDelay = 5000)
    public void sendOutboxEvents() {
        long start = System.currentTimeMillis();
        Pageable pageable = PageRequest.of(0, BATCH_SIZE);

        List<OutboxEvent> outboxEvents = outboxEventRepository.findByStatusOrderByIdAsc(Status.WAIT, pageable);
        log.info("[OutboxEventRelayProcessor] Send outbox events! size: {}, ids: {}", outboxEvents.size(), outboxEvents.stream().map(OutboxEvent::getId).toList());

        List<OutboxEventSuccessStatusUpdateCommand> successes = new ArrayList<>();
        List<OutboxEventFailStatusUpdateCommand> fails = new ArrayList<>();
        outboxEvents.forEach(event -> {
            try {
                kafkaProducer.sendSynchronously(event.getTopic(), event.getKey(), event.getPayload(), 10, TimeUnit.SECONDS);
                successes.add(new OutboxEventSuccessStatusUpdateCommand(event.getId(), LocalDateTime.now()));
            } catch (Exception e) {
                log.error("[OutboxEventRelayProcessor] Exception Occurred! events : {}", event, e);
                fails.add(new OutboxEventFailStatusUpdateCommand(event.getId(), e));
            }
        });

        outboxEventStatusUpdater.updateSuccessStatus(successes);
        outboxEventStatusUpdater.updateFailStatus(fails);

        long end = System.currentTimeMillis();
        log.info("[OutboxEventRelayProcessor] Completed send outbox events! Time taken: {} ms, Processed events: {}, Successes: {}, Fails: {}", (end - start), outboxEvents.size(), successes.size(), fails.size());
    }
}
