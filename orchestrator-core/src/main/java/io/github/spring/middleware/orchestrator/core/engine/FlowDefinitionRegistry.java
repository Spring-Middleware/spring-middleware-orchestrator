package io.github.spring.middleware.orchestrator.core.engine;

import io.github.spring.middleware.orchestrator.core.domain.FlowDefinition;
import io.github.spring.middleware.orchestrator.core.domain.FlowId;

public interface FlowDefinitionRegistry {

    FlowDefinition getFlowDefinition(FlowId flowId);

}
