package io.github.spring.middleware.orchestrator.core.port;

import io.github.spring.middleware.orchestrator.core.domain.TimeoutDefinition;
import io.github.spring.middleware.orchestrator.core.runtime.FlowExecutionTimeout;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

public interface TimeoutScheduler {

    Collection<FlowExecutionTimeout> getFlowExecutionTimeoutByDateTime(LocalDateTime dateTime);

    void scheduleTimeout(UUID flowExecutionId, TimeoutDefinition timeoutRedirection);

    public void removeTimeout(UUID flowExecutionId);
}