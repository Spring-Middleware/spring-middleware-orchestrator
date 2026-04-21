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


    @SuppressWarnings("unchecked")
    public <T, R> FlowExecutionActionRequest<?> executionActionRequest(FlowExecutionActionRequest<?> request) {
        ActionExecution.ActionExecutionBuilder actionExecutionBuilder = ActionExecution.builder()
                .actionName(request.getActionName())
                .executed(Boolean.FALSE);

        ExecutionContext executionContext = request.getExecutionContext();

        try {
            FunctionAction<T, R> functionAction =
                    actionRegistry.getAction(request.getActionName(), FunctionAction.class);

            ActionExecutorUtils.configureAction(functionAction, request.getActionDefinition());

            ActionExecutionContext actionExecutionContext = new ActionExecutionContext();

            T payload = parsePayload(request.getPayload(), functionAction, FunctionAction.class, functionAction::parsePayload);

            R result = functionAction.apply(
                    executionContext,
                    actionExecutionContext,
                    payload
            );

            if (!actionExecutionContext.isEmpty()) {
                actionExecutionBuilder.context(actionExecutionContext);
            }


            actionExecutionBuilder.executed(Boolean.TRUE);

            if (request.getActionDefinition().isFinalAction()) {
                endFlow(executionContext);
                return null;
            }

            FlowExecutionNextAction flowExecutionNextAction = flowNextActionResolver.getNextAction(
                    request.getFlowDefinition(),
                    executionContext,
                    request.getActionName(),
                    result
            );

            return FlowExecutionActionRequest.builder()
                    .flowDefinition(request.getFlowDefinition())
                    .executionContext(executionContext)
                    .actionDefinition(flowExecutionNextAction.getActionDefinition())
                    .payload(flowExecutionNextAction.getNextActionResolverResult().getResult())
                    .build();

        } catch (Exception ex) {
            processActionException(executionContext, request, actionExecutionBuilder, ex);
            return null;
        } finally {
            actionExecutionBuilder.executionDatetime(LocalDateTime.now());
            addActionExecutionToFlowExecution(executionContext, actionExecutionBuilder.build());
        }
    }

}

