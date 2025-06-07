package org.example.orderservice.kafka.producer;

public interface KafkaFailureCallback {
    void onFailure(Throwable exception);
}
