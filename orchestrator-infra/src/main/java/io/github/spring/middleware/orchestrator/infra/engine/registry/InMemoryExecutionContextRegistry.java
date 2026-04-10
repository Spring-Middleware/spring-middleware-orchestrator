package io.github.spring.middleware.orchestrator.infra.engine.registry;

import io.github.spring.middleware.orchestrator.core.runtime.ExecutionContext;
import io.github.spring.middleware.orchestrator.core.port.ExecutionContextRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class InMemoryExecutionContextRegistry implements ExecutionContextRegistry {

    private ConcurrentHashMap<UUID, ExecutionContext> contextMap = new ConcurrentHashMap<>();

    @Value("${orchestrator.execution-context.default-max-seconds-context-persisted:3600}")
    private Long defaultMaxSecondsContextPersisted;

    @Override
    public void addExecutionContext(ExecutionContext executionContext) {
        this.contextMap.put(executionContext.getFlowExecution().getId(), executionContext);
    }

    @Override
    public ExecutionContext remove(UUID flowExecutionId) {
        return this.contextMap.remove(flowExecutionId);
    }

    @Override
    public ExecutionContext get(UUID flowExecutionId) {
        return this.contextMap.get(flowExecutionId);
    }
}
