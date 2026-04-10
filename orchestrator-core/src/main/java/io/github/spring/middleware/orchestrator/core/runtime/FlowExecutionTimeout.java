package io.github.spring.middleware.orchestrator.core.runtime;

import io.github.spring.middleware.orchestrator.core.domain.TimeoutDefinition;

import java.util.UUID;

public record FlowExecutionTimeout(UUID flowExecutionId, TimeoutDefinition timeoutDefinition) {
}