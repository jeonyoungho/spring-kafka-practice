package org.example.orderservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class EventsConfiguration {
    private final ApplicationEventPublisher applicationEventPublisher;

    @Bean
    public InitializingBean eventsInitializer() {
        return () -> Events.setPublisher(applicationEventPublisher);
    }
}