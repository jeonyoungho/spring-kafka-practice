package org.example.orderservice.service.order;

import lombok.RequiredArgsConstructor;
import org.example.orderservice.config.Events;
import org.example.orderservice.domain.order.Order;
import org.example.orderservice.domain.order.OrderCreatedEvent;
import org.example.orderservice.domain.order.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public Long create(Long memberId, Long productId) {
        Order savedOrder = orderRepository.save(Order.create(memberId, productId));

        Long savedOrderId = savedOrder.getId();

        Events.raise(OrderCreatedEvent.create(savedOrderId));

        return savedOrder.getId();
    }
}
