package org.example.orderservice.domain.order;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public abstract class OrderEvent {
    protected Long orderId;
    protected OrderEventType eventType;

    protected OrderEvent(Long orderId, OrderEventType eventType) {
        this.orderId = orderId;
        this.eventType = eventType;
    }

}
