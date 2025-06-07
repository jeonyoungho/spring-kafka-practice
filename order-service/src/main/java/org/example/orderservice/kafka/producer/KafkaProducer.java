package org.example.orderservice.kafka.producer;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.example.orderservice.kafka.exception.KafkaBrokerUnavailableException;
import org.example.orderservice.kafka.exception.KafkaPublishException;
import org.example.orderservice.kafka.util.KafkaJsonConverter;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaJsonConverter kafkaJsonConverter;

    public void send(String topic, Object data) {
        send(topic, kafkaJsonConverter.serialize(data));
    }

    public void send(String topic, String data) {
        log.info("[Kafka][Producer] Topic: {}, Data: {} ", topic, data);
        kafkaTemplate.send(topic, data);
    }

    public void sendSynchronously(String topic, String key, String data, long timeout, TimeUnit unit) {
        log.debug("[Kafka][Producer] Topic: {}, Key: {}, Data: {} ", topic, key, data);

        try {
            kafkaTemplate.send(topic, key, data).get(timeout, unit);
        } catch (TimeoutException e) {
            throw new KafkaBrokerUnavailableException("Kafka broker timeout occurred!", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new KafkaPublishException("Interrupt occurred in kafka message publishing!", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();

            if (isConnectionError(cause)) {
                throw new KafkaBrokerUnavailableException("Could not be established kafka broker connection!", cause);
            } else {
                throw new KafkaPublishException("Could not publish message for kafka broker!", cause);
            }
        }
    }

    private boolean isConnectionError(Throwable cause) {
        return cause instanceof org.apache.kafka.common.errors.TimeoutException ||
                cause instanceof org.apache.kafka.common.errors.NetworkException ||
                cause instanceof java.net.ConnectException ||
                cause instanceof java.net.SocketTimeoutException;
    }

}
