package io.github.spring.middleware.orchestrator.infra.engine.registry;

import io.github.spring.middleware.orchestrator.core.runtime.ExecutionContext;
import io.github.spring.middleware.orchestrator.core.runtime.FlowExecution;
import io.github.spring.middleware.orchestrator.core.port.ExecutionContextStore;
import io.github.spring.middleware.orchestrator.infra.engine.repository.ExecutionContextPersistedDocument;
import io.github.spring.middleware.orchestrator.infra.engine.repository.MongoExecutionContextPersistedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PersistedExecutionContextStore implements ExecutionContextStore {

    private final MongoExecutionContextPersistedRepository executionContextPersistedRepository;

    public void persistExecutionContext(ExecutionContext executionContext) {
        if (!executionContext.getRuntimeContext().isEmpty()) {
            ExecutionContextPersisted executionContextPersisted = ExecutionContextPersisted.builder()
                    .flowExecutionId(executionContext.getFlowExecution().getId())
                    .context(executionContext.getRuntimeContext())
                    .build();
            executionContextPersistedRepository.save(toDocument(executionContextPersisted));
        }
    }

    public ExecutionContext loadContext(FlowExecution flowExecution) {
        ExecutionContextPersistedDocument executionContextPersistedDocument = executionContextPersistedRepository.findById(flowExecution.getId()).orElse(null);
        ExecutionContext executionContext = new ExecutionContext(flowExecution);
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
                .build();
    }

    private ExecutionContextPersisted toDomain(ExecutionContextPersistedDocument executionContextPersistedDocument) {
        return ExecutionContextPersisted.builder()
                .flowExecutionId(executionContextPersistedDocument.getId())
                .context(executionContextPersistedDocument.getContext())
                .build();
    }


}
