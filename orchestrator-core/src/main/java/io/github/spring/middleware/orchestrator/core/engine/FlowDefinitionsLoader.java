package io.github.spring.middleware.orchestrator.core.engine;

import io.github.spring.middleware.orchestrator.core.domain.FlowDefinition;

import java.util.Collection;

public interface FlowDefinitionsLoader {

    Collection<FlowDefinition> loadFlowDefinitions() throws FlowDefinitionsLoaderException;
}
