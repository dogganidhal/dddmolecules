package com.ndogga.dddmolecules;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

/**
 * Example implementation of DomainEventCollector for Spring Boot infrastructure.
 * 
 * This implementation:
 * 1. Extends ReflectionBasedEventCollector for annotation support
 * 2. Uses Spring's ApplicationEventPublisher for event publishing
 * 3. Ensures events are published after transaction commit
 * 4. Handles both sync and async event publishing
 */
@Slf4j
@Service
public class SpringDomainEventCollector extends ReflectionEventCollector {
    
    private final ApplicationEventPublisher applicationEventPublisher;
    
    public SpringDomainEventCollector(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
    
    @Override
    protected void publishEvents(List<Object> events) {
        if (events.isEmpty()) {
            return;
        }
        
        log.debug("Publishing {} domain events", events.size());
        
        // If we're in a transaction, register for after-commit publishing
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    log.debug("Transaction committed, publishing {} domain events", events.size());
                    publishEventsImmediately(events);
                }
                
                @Override
                public void afterCompletion(int status) {
                    if (status == STATUS_ROLLED_BACK) {
                        log.debug("Transaction rolled back, discarding {} domain events", events.size());
                    }
                }
            });
        } else {
            // No transaction, publish immediately
            publishEventsImmediately(events);
        }
    }
    
    private void publishEventsImmediately(List<Object> events) {
        for (Object event : events) {
            try {
                applicationEventPublisher.publishEvent(event);
                log.debug("Published domain event: {}", event.getClass().getSimpleName());
            } catch (Exception e) {
                log.error("Failed to publish domain event: {}", event.getClass().getSimpleName(), e);
                // Could implement retry logic, dead letter queue, etc.
            }
        }
    }
}

/**
 * Alternative implementation using a message queue (example with RabbitMQ/Kafka concepts)
 */
//@Service
//@Slf4j
//public class MessageQueueDomainEventCollector extends ReflectionBasedEventCollector {
//    
//    private final MessageProducer messageProducer; // Your message queue producer
//    private final ObjectMapper objectMapper;
//    
//    @Override
//    protected void publishEvents(List<Object> events) {
//        for (Object event : events) {
//            try {
//                String eventJson = objectMapper.writeValueAsString(event);
//                String routingKey = event.getClass().getSimpleName();
//                
//                messageProducer.send(routingKey, eventJson);
//                log.debug("Published domain event to queue: {}", routingKey);
//            } catch (Exception e) {
//                log.error("Failed to publish domain event to queue: {}", event.getClass().getSimpleName(), e);
//            }
//        }
//    }
//}"