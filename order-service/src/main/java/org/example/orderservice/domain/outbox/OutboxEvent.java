package org.example.orderservice.domain.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Entity
@Table(name = "oubox_event")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class OutboxEvent {

    private static final int MAX_STACK_TRACE_LENGTH = 2000;

    @Id
    private String id;

    @Column(name = "user_id", nullable = false)
    private String topic;

    @Column(name = "message_key", nullable = false)
    private String key;

    @Column(name = "message_payload", nullable = false)
    private String payload;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_modified_at")
    private LocalDateTime lastModifiedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "exception_stack_trace", columnDefinition = "TEXT")
    private String exceptionStackTrace;

    public enum Status {
        WAIT,
        SUCCESS,
        FAIL
    }

    public static OutboxEvent create(String id, String topic, String key, String payload) {
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.id = id;
        outboxEvent.topic = topic;
        outboxEvent.key = key;
        outboxEvent.payload = payload;
        outboxEvent.createdAt = LocalDateTime.now();
        outboxEvent.status = Status.WAIT;
        return outboxEvent;
    }

    public void changeSuccessStatus(LocalDateTime publishedAt) {
        this.status = Status.SUCCESS;
        this.publishedAt = publishedAt;
        this.lastModifiedAt = LocalDateTime.now();
    }

    public void changeFailStatus(Throwable throwable) {
        this.status = Status.FAIL;
        this.exceptionStackTrace = getStackTraceAsString(throwable);
        this.lastModifiedAt = LocalDateTime.now();
    }

    private String getStackTraceAsString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);

        StringBuffer buffer = sw.getBuffer();
        if (buffer.length() <= MAX_STACK_TRACE_LENGTH) {
            return buffer.toString();
        }

        return buffer.substring(0, MAX_STACK_TRACE_LENGTH - 3) + "...";
    }

}
