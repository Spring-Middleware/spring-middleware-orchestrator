package io.github.spring.middleware.orchestrator.core.port;

import io.github.spring.middleware.orchestrator.core.runtime.FlowExecution;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface FlowExecutionRegistry {

    Optional<FlowExecution> findById(UUID flowExecutionId);

    FlowExecution createFlowExecution(String flowId, Map<String, Object> initialContext);

}
