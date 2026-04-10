package io.github.spring.middleware.orchestrator.core.engine;

import io.github.spring.middleware.orchestrator.core.domain.TimeoutDefinition;
import io.github.spring.middleware.orchestrator.core.runtime.ExecutionContext;
import io.github.spring.middleware.orchestrator.core.runtime.FlowExecution;
import io.github.spring.middleware.orchestrator.core.runtime.FlowExecutionTimeout;
import io.github.spring.middleware.orchestrator.core.port.ExecutionContextRegistry;
import io.github.spring.middleware.orchestrator.core.port.ExecutionContextStore;
import io.github.spring.middleware.orchestrator.core.port.TimeoutScheduler;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

public class ExecutionContextManager {

    private final ExecutionContextRegistry executionContextRegistry;
    private final ExecutionContextStore executionContextStore;
    private final TimeoutScheduler timeoutScheduler;

    public ExecutionContextManager(ExecutionContextRegistry executionContextRegistry,
                                   ExecutionContextStore executionContextStore,
                                   TimeoutScheduler timeoutScheduler) {
        this.executionContextRegistry = executionContextRegistry;
        this.executionContextStore = executionContextStore;
        this.timeoutScheduler = timeoutScheduler;
    }

    public void addExecutionContext(ExecutionContext executionContext) {
        executionContextRegistry.addExecutionContext(executionContext);
    }

    public void removeExecutionContext(UUID flowExecutionId) {
        executionContextRegistry.remove(flowExecutionId);
        executionContextStore.removeContext(flowExecutionId);
        timeoutScheduler.removeTimeout(flowExecutionId);
    }

    public ExecutionContext getExecutionContext(UUID flowExecutionId) {
        return executionContextRegistry.get(flowExecutionId);
    }

    public Collection<FlowExecutionTimeout> getFlowExecutionTimeouts(LocalDateTime dateTime) {
        return timeoutScheduler.getFlowExecutionTimeoutByDateTime(dateTime);
    }

    public void persistExecutionContext(UUID flowExecutionId, TimeoutDefinition timeoutDefinition) {
        ExecutionContext executionContext = executionContextRegistry.remove(flowExecutionId);
        if (executionContext == null) {
            return;
        }
        executionContextStore.persistExecutionContext(executionContext);
        timeoutScheduler.scheduleTimeout(flowExecutionId, timeoutDefinition);
    }

    public ExecutionContext loadExecutionContext(FlowExecution flowExecution, boolean remove) {
        ExecutionContext executionContext = executionContextStore.loadContext(flowExecution);
        if (executionContext != null && remove) {
            executionContextStore.removeContext(flowExecution.getId());
            timeoutScheduler.removeTimeout(flowExecution.getId());
        }
        addExecutionContext(executionContext);
        return executionContext;
    }

}
