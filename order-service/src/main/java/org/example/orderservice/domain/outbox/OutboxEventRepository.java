package org.example.orderservice.domain.outbox;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    List<OutboxEvent> findAllByStatus(OutboxEvent.Status status);

    List<OutboxEvent> findByStatusOrderByIdAsc(OutboxEvent.Status status, Pageable pageable);
}
