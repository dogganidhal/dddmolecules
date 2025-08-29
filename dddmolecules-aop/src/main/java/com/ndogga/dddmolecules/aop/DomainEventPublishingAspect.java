package com.ndogga.dddmolecules.aop;

import com.ndogga.dddmolecules.AggregateRoot;
import com.ndogga.dddmolecules.DomainEventCollector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.util.ArrayList;

/**
 * Unified AOP Aspect that automatically publishes domain events from aggregates
 * when methods annotated with @PublishDomainEvents are executed.
 * 
 * This aspect works in conjunction with RepositoryEventTrackingAspect to:
 * 1. Start domain event tracking using DomainEventRegistry
 * 2. Execute the annotated method (during which repository aspects track aggregates)
 * 3. Collect all tracked aggregates with domain events from the registry
 * 4. Publish the events using the configured DomainEventCollector
 * 5. Ensure proper cleanup of ThreadLocal resources
 * 
 * The aspect automatically captures aggregates loaded/saved during method execution
 * through repository calls, eliminating the need to scan method parameters or fields.
 * 
 * Configuration properties:
 * - domain.events.aop.enabled: Enable/disable the aspect (default: true)
 * - domain.events.aop.log-performance: Log execution times (default: false)
 * 
 * Order is set to run after transaction management but before repository tracking.
 */
@Slf4j
@Aspect
@RequiredArgsConstructor
public class DomainEventPublishingAspect {
    
    private final DomainEventCollector domainEventCollector;
    
    @Around("@annotation(com.ndogga.dddmolecules.PublishDomainEvents)")
    public Object publishDomainEvents(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().getName();
        
        log.debug("Intercepting method {} for domain event publishing", methodName);
        
        // Start tracking domain events for this method execution
        DomainEventRegistry.startTracking();
        
        try {
            // Execute the original method (repository aspects will track aggregates)
            Object result = joinPoint.proceed();
            
            // If the result is an aggregate with events, register it too
            if (result instanceof AggregateRoot<?> aggregate) {
                DomainEventRegistry.registerAggregateWithEvents(aggregate);
            }
            
            // Get all tracked aggregates and publish their events
            var trackedAggregates = DomainEventRegistry.getTrackedAggregatesAndStopTracking();
            
            if (!trackedAggregates.isEmpty()) {
                long eventStartTime = System.currentTimeMillis();
                
                log.debug("Publishing domain events from {} tracked aggregates in method {}", 
                        trackedAggregates.size(), methodName);
                
                domainEventCollector.collectAndPublish(new ArrayList<>(trackedAggregates));
                
                if (log.isDebugEnabled()) {
                    long eventEndTime = System.currentTimeMillis();
                    log.debug("Event publishing took {} ms for method {}", 
                            eventEndTime - eventStartTime, methodName);
                }
            } else {
                log.debug("No aggregates with domain events found in method {}", methodName);
            }
            
            if (log.isDebugEnabled()) {
                long endTime = System.currentTimeMillis();
                log.debug("Total AOP processing took {} ms for method {}", 
                        endTime - startTime, methodName);
            }
            
            return result;
            
        } catch (Throwable ex) {
            // Ensure cleanup even if method fails
            log.warn("Exception in method {}, cleaning up domain event tracking", methodName);
            DomainEventRegistry.forceCleanup();
            throw ex;
        }
    }
}