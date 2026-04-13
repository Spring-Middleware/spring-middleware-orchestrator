package io.github.spring.middleware.orchestrator.core.engine;

import io.github.spring.middleware.orchestrator.core.domain.FlowId;
import io.github.spring.middleware.orchestrator.core.runtime.FlowExecution;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class FlowExecutionFactory<T> {

    public FlowExecution createFlowExecution(FlowId flowId, T context, String requestId) {
        return FlowExecution.builder()
                .id(UUID.randomUUID())
                .flowId(flowId)
                .context(context)
                .startDateTime(LocalDateTime.now())
                .requestId(requestId)
                .build();
    }

}
