package io.github.spring.middleware.orchestrator.core.runtime;

import io.github.spring.middleware.orchestrator.core.domain.FlowId;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class FlowExecution {

    private UUID id;
    private String requestId;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private FlowId flowId;
    private ExecutionStatus executionStatus;
    private Map<String, Object> context;
    private List<ActionExecution> actionExecutions;

}
