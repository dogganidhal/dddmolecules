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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * AOP Aspect that automatically publishes domain events from aggregates
 * when methods annotated with @PublishDomainEvents are executed.
 * 
 * This aspect:
 * 1. Executes the annotated method
 * 2. Scans method parameters, return value, and service fields for aggregates
 * 3. Collects domain events from found aggregates
 * 4. Publishes the events using the configured DomainEventCollector
 * 
 * Order is set to ensure this runs after transaction management.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
@Order(100) // Run after transaction management aspects
public class DomainEventPublishingAspect {
    
    private final DomainEventCollector domainEventCollector;
    
    @Around("@annotation(publishDomainEvents)")
    public Object publishDomainEvents(ProceedingJoinPoint joinPoint, PublishDomainEvents publishDomainEvents) throws Throwable {
        log.debug("Intercepting method {} for domain event publishing", joinPoint.getSignature().getName());
        
        // Collect aggregates before method execution
        List<AggregateRoot<?>> aggregatesBeforeExecution = collectAggregatesFromContext(joinPoint);
        
        // Execute the original method
        Object result = joinPoint.proceed();
        
        // Collect aggregates after method execution (including return value)
        List<AggregateRoot<?>> aggregatesAfterExecution = collectAggregatesFromContext(joinPoint);
        if (result instanceof AggregateRoot<?> aggregate) {
            aggregatesAfterExecution.add(aggregate);
        }
        
        // Collect and publish events from all found aggregates
        List<AggregateRoot<?>> allAggregates = new ArrayList<>();
        allAggregates.addAll(aggregatesBeforeExecution);
        allAggregates.addAll(aggregatesAfterExecution);
        
        if (!allAggregates.isEmpty()) {
            log.debug("Publishing domain events from {} aggregates", allAggregates.size());
            domainEventCollector.collectAndPublish(allAggregates);
        }
        
        return result;
    }
    
    /**
     * Collects all AggregateRoot instances from the method context.
     * This includes method parameters and service instance fields.
     */
    private List<AggregateRoot<?>> collectAggregatesFromContext(ProceedingJoinPoint joinPoint) {
        List<AggregateRoot<?>> aggregates = new ArrayList<>();
        
        // Scan method arguments
        Object[] args = joinPoint.getArgs();
        if (args != null) {
            for (Object arg : args) {
                collectAggregatesFromObject(arg, aggregates);
            }
        }
        
        // Scan service instance fields (for aggregates loaded in the method)
        Object target = joinPoint.getTarget();
        if (target != null) {
            collectAggregatesFromFields(target, aggregates);
        }
        
        return aggregates;
    }
    
    /**
     * Recursively collects aggregates from an object.
     */
    private void collectAggregatesFromObject(Object obj, List<AggregateRoot<?>> aggregates) {
        if (obj == null) {
            return;
        }
        
        if (obj instanceof AggregateRoot<?> aggregate) {
            aggregates.add(aggregate);
        } else if (obj instanceof Iterable<?> iterable) {
            // Handle collections of objects
            for (Object item : iterable) {
                collectAggregatesFromObject(item, aggregates);
            }
        } else if (obj.getClass().isArray()) {
            // Handle arrays
            Object[] array = (Object[]) obj;
            for (Object item : array) {
                collectAggregatesFromObject(item, aggregates);
            }
        }
        // Note: Could add more complex object traversal if needed
    }
    
    /**
     * Collects aggregates from service instance fields.
     * This is useful when aggregates are loaded and stored in service fields.
     */
    private void collectAggregatesFromFields(Object target, List<AggregateRoot<?>> aggregates) {
        try {
            Field[] fields = target.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(target);
                collectAggregatesFromObject(value, aggregates);
            }
        } catch (Exception e) {
            log.warn("Failed to scan fields for aggregates in {}: {}", target.getClass().getSimpleName(), e.getMessage());
        }
    }
}