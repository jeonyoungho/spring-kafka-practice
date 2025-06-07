package org.example.orderservice.controller;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.orderservice.controller.rqrs.OrderCreateRq;
import org.example.orderservice.service.order.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/orders")
public class OrderRestController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Long> create(@RequestBody OrderCreateRq rq) {
        Long orderId = orderService.create(rq.userId(), rq.productId());
        return ResponseEntity.ok(orderId);
    }

    @PostMapping("/bulk-create")
    public ResponseEntity<List<Long>> bulkCreate() throws InterruptedException {
        int size = 700;
        long memberId = 1L;
        ConcurrentLinkedQueue<Long> createdOrderIds = new ConcurrentLinkedQueue<>();
        CountDownLatch latch = new CountDownLatch(size);

        try (ExecutorService executor = Executors.newFixedThreadPool(size)) {
            for (long i = 1; i <= size; i++) {
                long productId = i;
                executor.submit(() -> {
                    try {
                        createdOrderIds.add(orderService.create(memberId, productId));
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
        }

        List<Long> result = createdOrderIds.stream().sorted().toList();
        log.info("Orders created: {}, size: {}", result, result.size());

        return ResponseEntity.ok(result);
    }
}
