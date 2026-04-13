package io.github.spring.middleware.orchestrator.infra.engine.registry;

import io.github.spring.middleware.orchestrator.core.domain.FlowDefinition;
import io.github.spring.middleware.orchestrator.core.domain.FlowId;
import io.github.spring.middleware.orchestrator.core.engine.FlowDefinitionRegistry;

import java.util.Map;


public class InMemoryFlowDefinitionRegistry implements FlowDefinitionRegistry {

    private final Map<FlowId, FlowDefinition> flowDefinitions;

    public InMemoryFlowDefinitionRegistry(Map<FlowId, FlowDefinition> flowDefinitions) {
        this.flowDefinitions = flowDefinitions;
    }

    @Override
    public FlowDefinition getFlowDefinition(FlowId flowId) {
        return flowDefinitions.get(flowId);
    }
}