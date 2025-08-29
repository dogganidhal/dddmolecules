package com.ndogga.dddmolecules.aop.autoconfiguration;


import com.ndogga.dddmolecules.DomainEventCollector;
import com.ndogga.dddmolecules.aop.DomainEventPublishingAspect;
import com.ndogga.dddmolecules.aop.RepositoryEventTrackingAspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.annotation.Order;

@AutoConfiguration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class AopDomainEventPublishingAutoConfiguration {

    @Bean
    @Order(50) // Run before the domain event publishing aspect (Order 100)
    public RepositoryEventTrackingAspect repositoryEventTrackingAspect() {
        return new RepositoryEventTrackingAspect();
    }

    @Bean
    @Order(100) // Run after transaction management aspects
    public DomainEventPublishingAspect domainEventPublishingAspect(DomainEventCollector domainEventCollector) {
        return new DomainEventPublishingAspect(domainEventCollector);
    }

}
