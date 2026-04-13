package io.github.spring.middleware.orchestrator.core.engine.action;

import io.github.spring.middleware.orchestrator.core.domain.ActionDefinition;
import io.github.spring.middleware.orchestrator.core.domain.ActionType;
import io.github.spring.middleware.orchestrator.core.domain.FlowDefinition;
import io.github.spring.middleware.orchestrator.core.runtime.ExecutionContext;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class FlowExecutionActionRequest<T> {

    private ExecutionContext executionContext;
    private FlowDefinition flowDefinition;
    private ActionDefinition actionDefinition;
    private T payload;

    public ActionType getActionType() {

        return actionDefinition.getActionType();
    }

    public String getActionName() {
        return actionDefinition.getActionName();
    }

    public ExecutionContext getExecutionContext() {

        return executionContext;
    }



}
