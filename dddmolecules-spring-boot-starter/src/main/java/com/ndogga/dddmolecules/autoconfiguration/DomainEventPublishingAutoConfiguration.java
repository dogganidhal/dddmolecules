package com.ndogga.dddmolecules.autoconfiguration;

import com.ndogga.dddmolecules.DomainEventCollector;
import com.ndogga.dddmolecules.SpringDomainEventCollector;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Configuration for enabling domain event publishing through AOP.
 * 
 * This configuration:
 * - Enables AspectJ auto proxy for @PublishDomainEvents annotation
 * - Only activates when AspectJ is on the classpath
 * - Can be disabled via property: domain.events.aop.enabled=false
 * 
 * To use this, ensure your Spring Boot application has:
 * 1. spring-boot-starter-aop dependency
 * 2. This configuration on the classpath
 * 3. A DomainEventCollector bean configured
 */
@Configuration
@ConditionalOnClass(name = "org.aspectj.lang.annotation.Aspect")
@ConditionalOnProperty(name = "domain.events.aop.enabled", havingValue = "true", matchIfMissing = true)
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class DomainEventPublishingAutoConfiguration {

    @Bean
    public DomainEventCollector domainEventCollector(ApplicationEventPublisher aep) {
        return new SpringDomainEventCollector(aep);
    }

}