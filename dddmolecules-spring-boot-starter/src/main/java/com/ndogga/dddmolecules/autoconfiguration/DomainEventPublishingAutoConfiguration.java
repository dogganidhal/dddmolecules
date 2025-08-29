package com.ndogga.dddmolecules.autoconfiguration;

import com.ndogga.dddmolecules.DomainEventCollector;
import com.ndogga.dddmolecules.SpringDomainEventCollector;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;

/**
 * Configuration for enabling domain event publishing through AOP.
 * <p>
 * This configuration:
 * - Enables AspectJ auto proxy for @PublishDomainEvents annotation
 * - Only activates when AspectJ is on the classpath
 * - Can be disabled via property: domain.events.aop.enabled=false
 * <p>
 * To use this, ensure your Spring Boot application has:
 * 1. spring-boot-starter-aop dependency
 * 2. This configuration on the classpath
 * 3. A DomainEventCollector bean configured
 */
@AutoConfiguration
public class DomainEventPublishingAutoConfiguration {

    @Bean
    public DomainEventCollector domainEventCollector(ApplicationEventPublisher aep) {
        return new SpringDomainEventCollector(aep);
    }

}