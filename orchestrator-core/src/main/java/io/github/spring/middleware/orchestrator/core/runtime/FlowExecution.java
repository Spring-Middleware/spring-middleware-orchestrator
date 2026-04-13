package io.github.spring.middleware.orchestrator.core.runtime;

import io.github.spring.middleware.orchestrator.core.domain.FlowId;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class FlowExecution<T> {

    private UUID id;
    private String requestId;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private FlowId flowId;
    private ExecutionStatus executionStatus;
    private T context;
    private List<ActionExecution> actionExecutions;


    public void addActionExecution(ActionExecution actionExecution) {
        if (actionExecutions == null) {
            actionExecutions = new ArrayList<>();
        }
        this.actionExecutions.add(actionExecution);
    }

}
