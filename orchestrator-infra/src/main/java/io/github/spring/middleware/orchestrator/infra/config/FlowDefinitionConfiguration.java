package io.github.spring.middleware.orchestrator.infra.config;

import io.github.spring.middleware.orchestrator.core.domain.FlowDefinition;
import io.github.spring.middleware.orchestrator.core.engine.FlowDefinitionRegistry;
import io.github.spring.middleware.orchestrator.core.engine.FlowDefinitionsLoader;
import io.github.spring.middleware.orchestrator.infra.engine.registry.InMemoryFlowDefinitionRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.stream.Collectors;

@Configuration
public class FlowDefinitionConfiguration {

    @Bean
    public FlowDefinitionRegistry flowDefinitionRegistry(FlowDefinitionsLoader flowDefinitionLoader) {
        return new InMemoryFlowDefinitionRegistry(flowDefinitionLoader.loadFlowDefinitions().stream().collect(Collectors.toMap(FlowDefinition::getFlowId, flowDefinition -> flowDefinition)));
    }
}