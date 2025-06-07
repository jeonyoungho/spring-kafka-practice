package org.example.orderservice.kafka.exception;

public class KafkaPublishException extends RuntimeException {

    public KafkaPublishException() {
    }

    public KafkaPublishException(String message) {
        super(message);
    }

    public KafkaPublishException(String message, Throwable cause) {
        super(message, cause);
    }

    public KafkaPublishException(Throwable cause) {
        super(cause);
    }

    public KafkaPublishException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
