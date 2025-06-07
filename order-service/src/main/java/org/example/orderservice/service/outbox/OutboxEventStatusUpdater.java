package org.example.orderservice.service.outbox;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.orderservice.domain.outbox.OutboxEvent;
import org.example.orderservice.domain.outbox.OutboxEventRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OutboxEventStatusUpdater {

    private final OutboxEventRepository outboxEventRepository;

    public record OutboxEventSuccessStatusUpdateCommand(String id, LocalDateTime publishedAt) { }

    public record OutboxEventFailStatusUpdateCommand(String id, Throwable throwable) { }

    @Transactional
    public void updateToSuccess(String id, LocalDateTime publishedAt) {
        updateToSuccess(Collections.singleton(
                new OutboxEventSuccessStatusUpdateCommand(id, publishedAt)
        ));
    }

    private void updateToSuccess(Collection<OutboxEventSuccessStatusUpdateCommand> commands) {
        List<OutboxEvent> outboxEvents = outboxEventRepository.findAllById(commands.stream()
                                                                                   .map(OutboxEventSuccessStatusUpdateCommand::id)
                                                                                   .toList());

        Map<String, LocalDateTime> publishedTimeMap =
                commands.stream()
                        .collect(Collectors.toMap(OutboxEventSuccessStatusUpdateCommand::id, OutboxEventSuccessStatusUpdateCommand::publishedAt));

        outboxEvents.forEach(it -> it.changeSuccessStatus(publishedTimeMap.getOrDefault(it.getId(), LocalDateTime.now())));
    }

    @Transactional
    public void updateToFail(String id, Throwable throwable) {
        updateToFail(Collections.singleton(
                new OutboxEventFailStatusUpdateCommand(id, throwable)
        ));
    }

    private void updateToFail(Collection<OutboxEventFailStatusUpdateCommand> commands) {
        List<OutboxEvent> outboxEvents = outboxEventRepository.findAllById(commands.stream()
                                                                                   .map(OutboxEventFailStatusUpdateCommand::id)
                                                                                   .toList());

        Map<String, Throwable> exceptionMap =
                commands.stream()
                        .collect(Collectors.toMap(OutboxEventFailStatusUpdateCommand::id, OutboxEventFailStatusUpdateCommand::throwable));

        outboxEvents.forEach(it -> it.changeFailStatus(exceptionMap.get(it.getId())));
    }

}
