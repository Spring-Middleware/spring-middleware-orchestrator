package io.github.spring.middleware.orchestrator.core.engine.action.exec;

import io.github.spring.middleware.orchestrator.core.domain.ActionDefinition;
import io.github.spring.middleware.orchestrator.core.engine.ExecutionContextManager;
import io.github.spring.middleware.orchestrator.core.engine.action.ConsumerAction;
import io.github.spring.middleware.orchestrator.core.engine.action.FlowExecutionActionRequest;
import io.github.spring.middleware.orchestrator.core.engine.action.FlowNextActionResolver;
import io.github.spring.middleware.orchestrator.core.engine.action.FunctionAction;
import io.github.spring.middleware.orchestrator.core.port.ActionRegistry;
import io.github.spring.middleware.orchestrator.core.port.FlowExecutionRegistry;
import io.github.spring.middleware.orchestrator.core.runtime.ActionExecution;
import io.github.spring.middleware.orchestrator.core.runtime.ActionExecutionContext;
import io.github.spring.middleware.orchestrator.core.runtime.ExecutionContext;
import io.github.spring.middleware.orchestrator.core.runtime.ExecutionStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ConsumerFlowActionExecutor extends CommonFlowActionExecutor {

    private final ActionRegistry actionRegistry;

    public ConsumerFlowActionExecutor(ActionRegistry actionRegistry,
                                      FlowExecutionRegistry flowExecutionRegistry,
                                      ExecutionContextManager executionContextManager) {
        super(executionContextManager, flowExecutionRegistry);
        this.actionRegistry = actionRegistry;
    }

    @SuppressWarnings("unchecked")
    public <T> void executionActionRequest(FlowExecutionActionRequest<T> flowExecutionActionRequest) {

        ActionExecution.ActionExecutionBuilder actionExecutionBuilder = ActionExecution.builder()
                .actionName(flowExecutionActionRequest.getActionName())
                .executed(Boolean.FALSE);

        ActionDefinition actionDefinition = flowExecutionActionRequest.getActionDefinition();
        ExecutionContext executionContext = flowExecutionActionRequest.getExecutionContext();

        try {
            ConsumerAction<T> consumerAction =
                    actionRegistry.getAction(flowExecutionActionRequest.getActionName(), ConsumerAction.class);

            ActionExecutorUtils.configureAction(consumerAction, actionDefinition);

            ActionExecutionContext actionExecutionContext = new ActionExecutionContext();

            T payload = parsePayload(flowExecutionActionRequest.getPayload(), consumerAction , ConsumerAction.class, consumerAction::parsePayload);

            if (!actionDefinition.isFinalAction()) {
                persistExecutionContext(executionContext, actionDefinition, payload);
            }

            consumerAction.consume(
                    executionContext,
                    actionExecutionContext,
                    payload
            );

            if (!actionExecutionContext.isEmpty()) {
                actionExecutionBuilder.context(actionExecutionContext);
            }

            actionExecutionBuilder.executed(Boolean.TRUE);
            actionExecutionBuilder.executionDatetime(LocalDateTime.now());

            if (actionDefinition.isFinalAction()) {
                endFlow(executionContext);
            }else{
                executionContext.getFlowExecution().setExecutionStatus(ExecutionStatus.SUSPENDED);
            }

        } catch (Exception ex) {
            processActionException(executionContext, flowExecutionActionRequest, actionExecutionBuilder, ex);
        } finally {
            addActionExecutionToFlowExecution(executionContext, actionExecutionBuilder.build());
        }
    }

}
