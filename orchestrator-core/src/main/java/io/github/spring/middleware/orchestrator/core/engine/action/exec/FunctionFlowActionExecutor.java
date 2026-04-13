package io.github.spring.middleware.orchestrator.core.engine.action.exec;

import io.github.spring.middleware.orchestrator.core.engine.ExecutionContextManager;
import io.github.spring.middleware.orchestrator.core.engine.action.FlowExecutionActionRequest;
import io.github.spring.middleware.orchestrator.core.engine.action.FlowNextActionResolver;
import io.github.spring.middleware.orchestrator.core.engine.action.FunctionAction;
import io.github.spring.middleware.orchestrator.core.port.ActionRegistry;
import io.github.spring.middleware.orchestrator.core.port.FlowExecutionRegistry;
import io.github.spring.middleware.orchestrator.core.runtime.ActionExecution;
import io.github.spring.middleware.orchestrator.core.runtime.ActionExecutionContext;
import io.github.spring.middleware.orchestrator.core.runtime.ExecutionContext;
import io.github.spring.middleware.orchestrator.core.runtime.FlowExecutionNextAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class FunctionFlowActionExecutor extends CommonFlowActionExecutor {

    private final ActionRegistry actionRegistry;
    private final FlowNextActionResolver flowNextActionResolver;

    public FunctionFlowActionExecutor(
            ActionRegistry actionRegistry,
            FlowNextActionResolver flowNextActionResolver,
            FlowExecutionRegistry flowExecutionRegistry,
            ExecutionContextManager executionContextManager
    ) {
        super(executionContextManager, flowExecutionRegistry);
        this.actionRegistry = actionRegistry;
        this.flowNextActionResolver = flowNextActionResolver;
    }


    public <T, R> FlowExecutionActionRequest executionActionRequest(FlowExecutionActionRequest flowExecutionActionRequest) {
        ActionExecution.ActionExecutionBuilder actionExecutionBuilder = ActionExecution.builder()
                .actionName(flowExecutionActionRequest.getActionName())
                .executed(Boolean.FALSE);

        ExecutionContext executionContext = flowExecutionActionRequest.getExecutionContext();

        try {
            FunctionAction<T, R> functionAction =
                    actionRegistry.getAction(flowExecutionActionRequest.getActionName(), FunctionAction.class);

            ActionExecutorUtils.configureAction(functionAction, flowExecutionActionRequest.getActionDefinition());

            ActionExecutionContext actionExecutionContext = new ActionExecutionContext();

            R result = functionAction.apply(
                    executionContext,
                    actionExecutionContext,
                    (T) flowExecutionActionRequest.getPayload()
            );

            if (!actionExecutionContext.isEmpty()) {
                actionExecutionBuilder.context(actionExecutionContext);
            }

            actionExecutionBuilder.executed(Boolean.TRUE);
            actionExecutionBuilder.executionDatetime(LocalDateTime.now());

            ActionExecution actionExecution = actionExecutionBuilder.build();
            addActionExecutionToFlowExecution(executionContext, actionExecution);

            if (flowExecutionActionRequest.getActionDefinition().isFinalAction()) {
                endFlow(executionContext);
                return null;
            }

            FlowExecutionNextAction flowExecutionNextAction = flowNextActionResolver.getNextAction(
                    flowExecutionActionRequest.getFlowDefinition(),
                    executionContext,
                    flowExecutionActionRequest.getActionName(),
                    result
            );

            return FlowExecutionActionRequest.builder()
                    .flowDefinition(flowExecutionActionRequest.getFlowDefinition())
                    .executionContext(executionContext)
                    .actionDefinition(flowExecutionNextAction.getActionDefinition())
                    .payload(flowExecutionNextAction.getNextActionResolverResult().getResult())
                    .build();

        } catch (Exception ex) {
            processActionException(executionContext, flowExecutionActionRequest, actionExecutionBuilder, ex);
            actionExecutionBuilder.executionDatetime(LocalDateTime.now());
            ActionExecution actionExecution = actionExecutionBuilder.build();
            addActionExecutionToFlowExecution(executionContext, actionExecution);
            return null;
        }
    }

}

