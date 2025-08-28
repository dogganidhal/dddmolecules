package com.ndogga.dddmolecules.aop;

import com.ndogga.dddmolecules.AggregateRoot;
import com.ndogga.dddmolecules.DomainEventCollector;
import com.ndogga.dddmolecules.PublishDomainEvents;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Advanced AOP Aspect for domain event publishing that tracks aggregate states
 * and only publishes events from aggregates that have been modified.
 * 
 * This version:
 * 1. Takes snapshots of aggregate states before method execution
 * 2. Executes the annotated method
 * 3. Identifies which aggregates have new domain events
 * 4. Publishes events only from modified aggregates
 * 
 * This is more efficient for services that work with multiple aggregates
 * but only modify some of them.
 */
@Slf4j
@Aspect
@Component("advancedDomainEventPublishingAspect")
@RequiredArgsConstructor
@Order(100)
public class AdvancedDomainEventPublishingAspect {
    
    private final DomainEventCollector domainEventCollector;
    
    @Around("@annotation(publishDomainEvents)")
    public Object publishDomainEventsAdvanced(ProceedingJoinPoint joinPoint, PublishDomainEvents publishDomainEvents) throws Throwable {
        log.debug("Advanced intercepting method {} for domain event publishing", joinPoint.getSignature().getName());
        
        // Collect all aggregates and snapshot their event counts
        Set<AggregateRoot<?>> allAggregates = new HashSet<>();
        Map<AggregateRoot<?>, Integer> eventCountsBeforeExecution = new HashMap<>();
        
        collectAggregatesFromContext(joinPoint, allAggregates);
        
        // Snapshot event counts before execution
        for (AggregateRoot<?> aggregate : allAggregates) {
            eventCountsBeforeExecution.put(aggregate, aggregate.getDomainEvents().size());
        }
        
        // Execute the original method
        Object result = joinPoint.proceed();
        
        // Add result to aggregates if it's an aggregate
        if (result instanceof AggregateRoot<?> resultAggregate) {
            allAggregates.add(resultAggregate);
            // Don't snapshot this one as it's newly created/returned
        }
        
        // Re-scan for any new aggregates after method execution
        collectAggregatesFromContext(joinPoint, allAggregates);
        
        // Identify aggregates with new events
        List<AggregateRoot<?>> aggregatesWithNewEvents = new ArrayList<>();
        
        for (AggregateRoot<?> aggregate : allAggregates) {
            Integer previousEventCount = eventCountsBeforeExecution.get(aggregate);
            int currentEventCount = aggregate.getDomainEvents().size();
            
            // If we don't have a previous count or the count increased, include it
            if (previousEventCount == null || currentEventCount > previousEventCount) {
                if (aggregate.hasDomainEvents()) {
                    aggregatesWithNewEvents.add(aggregate);
                }
            }
        }
        
        // Publish events from modified aggregates only
        if (!aggregatesWithNewEvents.isEmpty()) {
            log.debug("Publishing domain events from {} modified aggregates out of {} total", 
                    aggregatesWithNewEvents.size(), allAggregates.size());
            domainEventCollector.collectAndPublish(aggregatesWithNewEvents);
        }
        
        return result;
    }
    
    /**
     * Collects all AggregateRoot instances from the method context recursively.
     */
    private void collectAggregatesFromContext(ProceedingJoinPoint joinPoint, Set<AggregateRoot<?>> aggregates) {
        // Scan method arguments
        Object[] args = joinPoint.getArgs();
        if (args != null) {
            for (Object arg : args) {
                collectAggregatesRecursively(arg, aggregates, new HashSet<>());
            }
        }
    }
    
    /**
     * Recursively collects aggregates from an object with cycle detection.
     */
    private void collectAggregatesRecursively(Object obj, Set<AggregateRoot<?>> aggregates, Set<Object> visited) {
        if (obj == null || visited.contains(obj)) {
            return;
        }
        
        visited.add(obj);
        
        if (obj instanceof AggregateRoot<?> aggregate) {
            aggregates.add(aggregate);
        } else if (obj instanceof Iterable<?> iterable) {
            for (Object item : iterable) {
                collectAggregatesRecursively(item, aggregates, visited);
            }
        } else if (obj instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                collectAggregatesRecursively(entry.getKey(), aggregates, visited);
                collectAggregatesRecursively(entry.getValue(), aggregates, visited);
            }
        } else if (obj.getClass().isArray()) {
            Object[] array = (Object[]) obj;
            for (Object item : array) {
                collectAggregatesRecursively(item, aggregates, visited);
            }
        }
        // Could add more sophisticated object traversal using reflection if needed
    }
}