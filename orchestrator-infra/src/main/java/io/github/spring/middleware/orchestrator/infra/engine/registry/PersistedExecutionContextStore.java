package io.github.spring.middleware.orchestrator.infra.engine.registry;

import io.github.spring.middleware.orchestrator.core.port.ExecutionContextStore;
import io.github.spring.middleware.orchestrator.core.runtime.ExecutionContext;
import io.github.spring.middleware.orchestrator.core.runtime.FlowExecution;
import io.github.spring.middleware.orchestrator.infra.engine.repository.ExecutionContextPersistedDocument;
import io.github.spring.middleware.orchestrator.infra.engine.repository.MongoExecutionContextPersistedRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PersistedExecutionContextStore implements ExecutionContextStore {

    private final MongoExecutionContextPersistedRepository executionContextPersistedRepository;

    public <T> void persistExecutionContext(ExecutionContext executionContext, String actionName, T payload) {
        if (!executionContext.getRuntimeContext().isEmpty() || payload != null) {
            ExecutionContextPersisted executionContextPersisted = ExecutionContextPersisted.builder()
                    .flowExecutionId(executionContext.getFlowExecution().getId())
                    .actionName(actionName)
                    .context(executionContext.getRuntimeContext())
                    .payload(payload)
                    .build();
            ExecutionContextPersistedDocument saved =
                    executionContextPersistedRepository.save(toDocument(executionContextPersisted));
            log.info("Execution context persisted with id={}", saved.getId());
        }
    }

    public ExecutionContext loadContext(FlowExecution flowExecution) {
        ExecutionContextPersistedDocument executionContextPersistedDocument = executionContextPersistedRepository.findById(flowExecution.getId()).orElse(null);
        ExecutionContext executionContext = new ExecutionContext(flowExecution, executionContextPersistedDocument.getPayload());
        if (executionContextPersistedDocument != null) {
            ExecutionContextPersisted executionContextPersisted = toDomain(executionContextPersistedDocument);
            Map<String, Object> context = executionContextPersisted.getContext();
            context.entrySet().forEach(entry -> executionContext.put(entry.getKey(), entry.getValue()));
        }
        return executionContext;
    }


    public void removeContext(UUID flowExecutionId) {
        executionContextPersistedRepository.deleteById(flowExecutionId);
    }


    private ExecutionContextPersistedDocument toDocument(ExecutionContextPersisted executionContextPersisted) {
        return ExecutionContextPersistedDocument.builder()
                .id(executionContextPersisted.getFlowExecutionId())
                .context(executionContextPersisted.getContext())
                .creationDateTime(executionContextPersisted.getCreationDateTime())
                .actionName(executionContextPersisted.getActionName())
                .payload(executionContextPersisted.getPayload())
                .build();
    }

    private ExecutionContextPersisted toDomain(ExecutionContextPersistedDocument executionContextPersistedDocument) {
        return ExecutionContextPersisted.builder()
                .flowExecutionId(executionContextPersistedDocument.getId())
                .context(executionContextPersistedDocument.getContext())
                .creationDateTime(executionContextPersistedDocument.getCreationDateTime())
                .actionName(executionContextPersistedDocument.getActionName())
                .payload(executionContextPersistedDocument.getPayload())
                .build();
    }


}
