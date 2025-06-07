package org.example.orderservice.domain.outbox;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OutboxEventStatusUpdater {

    private final OutboxEventRepository outboxEventRepository;

    public record OutboxEventSuccessStatusUpdateCommand(Long id, LocalDateTime publishedAt) {}

    public record OutboxEventFailStatusUpdateCommand(Long id, Throwable throwable) {}

    @Transactional
    public void updateSuccessStatus(Collection<OutboxEventSuccessStatusUpdateCommand> commands) {
        List<OutboxEvent> outboxEvents = outboxEventRepository.findAllById(commands.stream()
                                                                                   .map(OutboxEventSuccessStatusUpdateCommand::id)
                                                                                   .toList());

        Map<Long, LocalDateTime> publishedTimeMap =
                commands.stream()
                        .collect(Collectors.toMap(OutboxEventSuccessStatusUpdateCommand::id, OutboxEventSuccessStatusUpdateCommand::publishedAt));

        outboxEvents.forEach(it -> it.changeSuccessStatus(publishedTimeMap.getOrDefault(it.getId(), LocalDateTime.now())));
    }

    @Transactional
    public void updateFailStatus(Collection<OutboxEventFailStatusUpdateCommand> commands) {
        List<OutboxEvent> outboxEvents = outboxEventRepository.findAllById(commands.stream()
                                                                                   .map(OutboxEventFailStatusUpdateCommand::id)
                                                                                   .toList());

        Map<Long, Throwable> exceptionMap =
                commands.stream()
                        .collect(Collectors.toMap(OutboxEventFailStatusUpdateCommand::id, OutboxEventFailStatusUpdateCommand::throwable));

        outboxEvents.forEach(it -> it.changeFailStatus(exceptionMap.get(it.getId())));
    }

}
