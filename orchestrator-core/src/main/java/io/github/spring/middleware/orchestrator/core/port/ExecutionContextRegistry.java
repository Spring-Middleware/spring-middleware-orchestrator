package io.github.spring.middleware.orchestrator.core.port;

import io.github.spring.middleware.orchestrator.core.runtime.ExecutionContext;

import java.util.UUID;

public interface ExecutionContextRegistry {

    void addExecutionContext(ExecutionContext executionContext);

    ExecutionContext remove(UUID flowExecutionId);

    ExecutionContext get(UUID flowExecutionId);

}
