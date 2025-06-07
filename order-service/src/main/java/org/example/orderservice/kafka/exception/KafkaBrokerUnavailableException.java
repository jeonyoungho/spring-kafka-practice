package org.example.orderservice.kafka.exception;

public class KafkaBrokerUnavailableException extends RuntimeException{

    public KafkaBrokerUnavailableException() {
    }

    public KafkaBrokerUnavailableException(String message) {
        super(message);
    }

    public KafkaBrokerUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public KafkaBrokerUnavailableException(Throwable cause) {
        super(cause);
    }

    public KafkaBrokerUnavailableException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
