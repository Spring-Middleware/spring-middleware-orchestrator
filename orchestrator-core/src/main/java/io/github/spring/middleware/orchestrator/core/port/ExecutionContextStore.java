package io.github.spring.middleware.orchestrator.core.port;

import io.github.spring.middleware.orchestrator.core.runtime.ExecutionContext;
import io.github.spring.middleware.orchestrator.core.runtime.FlowExecution;

import java.util.UUID;

public interface ExecutionContextStore {

    <T> void persistExecutionContext(ExecutionContext executionContext, String actionName, T payload);

    ExecutionContext loadContext(FlowExecution flowExecution);

    void removeContext(UUID flowExecutionId);

}
