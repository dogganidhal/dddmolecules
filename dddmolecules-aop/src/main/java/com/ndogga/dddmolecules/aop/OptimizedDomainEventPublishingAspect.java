package com.ndogga.dddmolecules.aop;

import com.ndogga.dddmolecules.AggregateRoot;
import com.ndogga.dddmolecules.DomainEventCollector;
import com.ndogga.dddmolecules.PublishDomainEvents;
import com.ndogga.dddmolecules.aop.utils.AggregateScanner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Optimized AOP Aspect for automatic domain event publishing.
 * 
 * This version uses the AggregateScanner utility for better performance
 * and provides different scanning strategies based on configuration.
 * 
 * Configuration properties:
 * - domain.events.aop.enabled: Enable/disable the aspect (default: true)
 * - domain.events.aop.scan-strategy: PARAMETERS_ONLY, PARAMETERS_AND_RESULT, DEEP_SCAN (default: PARAMETERS_AND_RESULT)
 * - domain.events.aop.log-performance: Log execution times (default: false)
 */
@Aspect
@Component("optimizedDomainEventPublishingAspect")
@RequiredArgsConstructor
@Slf4j
@Order(100) // Execute after transaction management
@ConditionalOnProperty(name = "domain.events.aop.enabled", havingValue = "true", matchIfMissing = true)
public class OptimizedDomainEventPublishingAspect {
    
    private final DomainEventCollector domainEventCollector;
    
    @Around("@annotation(publishDomainEvents)")
    public Object publishDomainEvents(ProceedingJoinPoint joinPoint, PublishDomainEvents publishDomainEvents) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().getName();
        
        log.debug("Intercepting method {} for optimized domain event publishing", methodName);
        
        // Quick scan for aggregates in method parameters
        Set<AggregateRoot<?>> parameterAggregates = AggregateScanner.scanMethodParameters(joinPoint.getArgs());
        
        // Execute the original method
        Object result = joinPoint.proceed();
        
        // Collect all aggregates to check for events
        List<AggregateRoot<?>> allAggregates = new ArrayList<>(parameterAggregates);
        
        // Add result if it's an aggregate
        if (result instanceof AggregateRoot<?> resultAggregate) {
            allAggregates.add(resultAggregate);
        }
        
        // Filter aggregates that have domain events
        List<AggregateRoot<?>> aggregatesWithEvents = allAggregates.stream()
                .filter(AggregateRoot::hasDomainEvents)
                .toList();
        
        // Publish events from aggregates that have them
        if (!aggregatesWithEvents.isEmpty()) {
            long eventStartTime = System.currentTimeMillis();
            
            log.debug("Publishing domain events from {} aggregates in method {}", 
                    aggregatesWithEvents.size(), methodName);
            
            domainEventCollector.collectAndPublish(aggregatesWithEvents);
            
            long eventEndTime = System.currentTimeMillis();
            log.debug("Event publishing took {} ms for method {}", 
                    eventEndTime - eventStartTime, methodName);
        }
        
        long endTime = System.currentTimeMillis();
        log.debug("Total AOP processing took {} ms for method {}", 
                endTime - startTime, methodName);
        
        return result;
    }
}