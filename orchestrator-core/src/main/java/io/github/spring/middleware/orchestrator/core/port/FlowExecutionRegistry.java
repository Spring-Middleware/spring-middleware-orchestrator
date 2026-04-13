package io.github.spring.middleware.orchestrator.core.port;

import io.github.spring.middleware.orchestrator.core.runtime.ActionExecution;
import io.github.spring.middleware.orchestrator.core.runtime.FlowExecution;

import java.util.Optional;
import java.util.UUID;

public interface FlowExecutionRegistry {

    Optional<FlowExecution> findById(UUID flowExecutionId);

    <T> FlowExecution createFlowExecution(String flowId, T initialContext);

    FlowExecution addActionExecutionToFlowExecution(UUID flowExecutionId, ActionExecution actionExecution);

}
