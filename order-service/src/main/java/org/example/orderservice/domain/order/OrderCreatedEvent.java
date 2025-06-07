package org.example.orderservice.domain.order;

import lombok.Getter;

@Getter
public class OrderCreatedEvent extends OrderEvent {

    private OrderCreatedEvent(Long orderId) {
        super(orderId, OrderEventType.CREATE);
    }

    public static OrderCreatedEvent create(Long orderId) {
        return new OrderCreatedEvent(orderId);
    }

}
