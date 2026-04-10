package io.github.spring.middleware.orchestrator.core.engine;

import io.github.spring.middleware.orchestrator.core.domain.FlowId;
import io.github.spring.middleware.orchestrator.core.runtime.FlowExecution;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
public class FlowExecutionFactory {

    public FlowExecution createFlowExecution(FlowId flowId, Map<String, Object> context, String requestId) {
        return FlowExecution.builder()
                .id(UUID.randomUUID())
                .flowId(flowId)
                .context(context)
                .startDateTime(LocalDateTime.now())
                .requestId(requestId)
                .build();
    }

}
