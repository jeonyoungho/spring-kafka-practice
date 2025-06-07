package org.example.orderservice.kafka.producer;

import org.springframework.kafka.support.SendResult;

public interface KafkaSuccessCallback {
    void onSuccess(SendResult<String, String> result);
}

