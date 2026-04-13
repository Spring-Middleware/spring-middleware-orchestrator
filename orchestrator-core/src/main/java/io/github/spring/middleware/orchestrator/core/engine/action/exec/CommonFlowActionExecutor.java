package io.github.spring.middleware.orchestrator.core.engine.action.exec;

import io.github.spring.middleware.exception.ExceptionUtils;
import io.github.spring.middleware.orchestrator.core.domain.ActionDefinition;
import io.github.spring.middleware.orchestrator.core.engine.ExecutionContextManager;
import io.github.spring.middleware.orchestrator.core.engine.action.FlowExecutionActionRequest;
import io.github.spring.middleware.orchestrator.core.port.FlowExecutionRegistry;
import io.github.spring.middleware.orchestrator.core.runtime.ActionExecution;
import io.github.spring.middleware.orchestrator.core.runtime.ExecutionContext;
import io.github.spring.middleware.orchestrator.core.runtime.ExecutionStatus;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
public class CommonFlowActionExecutor {

    protected final ExecutionContextManager executionContextManager;
    protected final FlowExecutionRegistry flowExecutionRegistry;

    public CommonFlowActionExecutor(ExecutionContextManager executionContextManager, FlowExecutionRegistry flowExecutionRegistry) {
        this.flowExecutionRegistry = flowExecutionRegistry;
        this.executionContextManager = executionContextManager;
    }

    protected void endFlow(ExecutionContext executionContext) {
        executionContext.getFlowExecution().setExecutionStatus(ExecutionStatus.EXECUTED);
        executionContext.getFlowExecution().setEndDateTime(LocalDateTime.now());
    }

    protected void addActionExecutionToFlowExecution(ExecutionContext executionContext, ActionExecution
            actionExecution) {
        executionContext.addActionExecution(actionExecution);
        flowExecutionRegistry.addActionExecutionToFlowExecution(executionContext.getFlowExecution().getId(), actionExecution);
    }

    protected void processActionException(ExecutionContext executionContext,
                                          FlowExecutionActionRequest flowExecutionActionRequest,
                                          ActionExecution.ActionExecutionBuilder actionExecutionBuilder, Exception ex) {

        log.error(STR."Error on action \{flowExecutionActionRequest.getActionDefinition().getActionName()}", ex);
        executionContext.getFlowExecution().setExecutionStatus(ExecutionStatus.ERROR);
        actionExecutionBuilder.error(ExceptionUtils.getNotNullMessage(ex));
    }

    protected void persistExecutionContext(ExecutionContext executionContext, ActionDefinition actionDefinition) {
        executionContextManager.persistExecutionContext(
                executionContext.getFlowExecution().getId(),
                actionDefinition.getTimeout()
        );
    }


}
