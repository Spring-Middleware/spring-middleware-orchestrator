package io.github.spring.middleware.orchestrator.core.runtime;

import io.github.spring.middleware.orchestrator.core.domain.FlowId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FlowExecution<T,C> {

    private UUID id;
    private String requestId;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private FlowId flowId;
    private ExecutionStatus executionStatus;
    private T input;
    private C context;
    private Map<String, ActionExecutionOrder> actionExecutions;


    public void addActionExecution(ActionExecutionOrder actionExecution) {
        if (actionExecutions == null) {
            actionExecutions = new HashMap<>();
        }
        this.actionExecutions.put(actionExecution.getActionName(), actionExecution);
    }

    public ActionExecution getActionExecution(String actionName) {
        return actionExecutions.get(actionName).getActionExecution();
    }

}
