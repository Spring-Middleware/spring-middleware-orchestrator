package io.github.spring.middleware.orchestrator.core.runtime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActionExecutionOrder {

    private Integer executionOrder;
    private ActionExecution actionExecution;

    public String getActionName() {
        return actionExecution.getActionName();
    }


}
