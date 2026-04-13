package io.github.spring.middleware.orchestrator.core.engine.action.exec;

import io.github.spring.middleware.orchestrator.core.engine.ExecutionContextManager;
import io.github.spring.middleware.orchestrator.core.engine.action.FlowExecutionActionRequest;
import io.github.spring.middleware.orchestrator.core.engine.action.FlowNextActionResolver;
import io.github.spring.middleware.orchestrator.core.port.FlowExecutionRegistry;
import io.github.spring.middleware.orchestrator.core.runtime.ActionExecution;
import io.github.spring.middleware.orchestrator.core.runtime.ExecutionContext;
import io.github.spring.middleware.orchestrator.core.runtime.FlowExecutionNextAction;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ResumeFlowActionExecutor extends CommonFlowActionExecutor {

    private final FlowNextActionResolver flowNextActionResolver;

    public ResumeFlowActionExecutor(FlowNextActionResolver flowNextActionResolver,
                                    FlowExecutionRegistry flowExecutionRegistry,
                                    ExecutionContextManager executionContextManager) {
        super(executionContextManager, flowExecutionRegistry);
        this.flowNextActionResolver = flowNextActionResolver;
    }

    public FlowExecutionActionRequest<?> executionActionRequest(FlowExecutionActionRequest<?> flowExecutionActionRequest) {

        ActionExecution.ActionExecutionBuilder actionExecutionBuilder = ActionExecution.builder()
                .actionName(flowExecutionActionRequest.getActionName())
                .executed(Boolean.TRUE)
                .executionDatetime(LocalDateTime.now());

        ExecutionContext executionContext = flowExecutionActionRequest.getExecutionContext();

        try {
            if (flowExecutionActionRequest.getActionDefinition().isFinalAction()) {
                endFlow(executionContext);
                return null;
            }

            FlowExecutionNextAction flowExecutionNextAction = flowNextActionResolver.getNextAction(
                    flowExecutionActionRequest.getFlowDefinition(),
                    executionContext,
                    flowExecutionActionRequest.getActionName(),
                    flowExecutionActionRequest.getPayload()
            );

            return FlowExecutionActionRequest.builder()
                    .flowDefinition(flowExecutionActionRequest.getFlowDefinition())
                    .executionContext(executionContext)
                    .actionDefinition(flowExecutionNextAction.getActionDefinition())
                    .payload(flowExecutionNextAction.getNextActionResolverResult().getResult())
                    .build();

        } catch (Exception ex) {
            processActionException(executionContext, flowExecutionActionRequest, actionExecutionBuilder, ex);
            return null;
        } finally {
            addActionExecutionToFlowExecution(executionContext, actionExecutionBuilder.build());
        }
    }
}
