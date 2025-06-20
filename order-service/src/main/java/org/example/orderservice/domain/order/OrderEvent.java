package org.example.orderservice.domain.order;

import lombok.Getter;
import lombok.ToString;
import org.example.orderservice.util.UUIDGenerator;

@ToString
@Getter
public abstract class OrderEvent {
    protected Long orderId;
    protected OrderEventType eventType;
    protected String outboxEventId;

    protected OrderEvent(Long orderId, OrderEventType eventType) {
        this.orderId = orderId;
        this.eventType = eventType;
        this.outboxEventId = UUIDGenerator.generate();
    }

}
