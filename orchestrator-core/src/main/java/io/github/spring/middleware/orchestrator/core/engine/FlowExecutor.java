package io.github.spring.middleware.orchestrator.core.engine;

import io.github.spring.middleware.orchestrator.core.domain.ActionDefinition;
import io.github.spring.middleware.orchestrator.core.domain.ActionType;
import io.github.spring.middleware.orchestrator.core.domain.FlowDefinition;
import io.github.spring.middleware.orchestrator.core.domain.FlowId;
import io.github.spring.middleware.orchestrator.core.domain.params.NextActionResolverParams;
import io.github.spring.middleware.orchestrator.core.engine.action.FlowExecutionActionRequest;
import io.github.spring.middleware.orchestrator.core.engine.action.exec.FlowActionExecutor;
import io.github.spring.middleware.orchestrator.core.port.FlowExecutionRegistry;
import io.github.spring.middleware.orchestrator.core.port.NextActionResolverRegistry;
import io.github.spring.middleware.orchestrator.core.runtime.ActionException;
import io.github.spring.middleware.orchestrator.core.runtime.ExecutionContext;
import io.github.spring.middleware.orchestrator.core.runtime.ExecutionStatus;
import io.github.spring.middleware.orchestrator.core.runtime.FlowExecution;
import io.github.spring.middleware.orchestrator.core.runtime.FlowExecutionTimeout;
import io.github.spring.middleware.orchestrator.core.runtime.FlowTrigger;
import io.github.spring.middleware.orchestrator.core.runtime.NextActionResolverResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Component
@RequiredArgsConstructor
public class FlowExecutor {

    private final FlowDefinitionRegistry flowDefinitionRegistry;
    private final FlowExecutionRegistry flowExecutionRegistry;
    private final ExecutionContextManager executionContextManager;
    private final FlowActionExecutor flowActionExecutor;
    private final NextActionResolverRegistry nextActionResolverRegistry;
    private final Executor flowExecutorTaskExecutor;


    public UUID startFlow(FlowTrigger flowTrigger) {
        FlowExecution flowExecution = flowExecutionRegistry.createFlowExecution(
                flowTrigger.getFlowId(),
                flowTrigger.getPayload()
        );

        ExecutionContext executionContext = new ExecutionContext(flowExecution, flowTrigger.getPayload());
        executionContextManager.addExecutionContext(executionContext);

        FlowDefinition flowDefinition = flowDefinitionRegistry.getFlowDefinition(
                new FlowId(flowTrigger.getFlowId())
        );

        if (flowDefinition == null) {
            executionContextManager.removeExecutionContext(flowExecution.getId());
            throw new IllegalStateException(
                    STR."Flow definition not found for flowId: \{flowTrigger.getFlowId()}"
            );
        }else{


        }

        ActionDefinition firstAction = flowDefinition.getFirstAction();
        if (firstAction == null) {
            executionContextManager.removeExecutionContext(flowExecution.getId());
            throw new IllegalStateException(
                    STR."First action not defined for flowId: \{flowTrigger.getFlowId()}"
            );
        }

        FlowExecutionActionRequest<?> flowExecutionActionRequest = FlowExecutionActionRequest.builder()
                .flowDefinition(flowDefinition)
                .actionDefinition(firstAction)
                .executionContext(executionContext)
                .payload(flowTrigger.getPayload())
                .build();

        executeFlowAsync(flowExecutionActionRequest, flowExecution);
        return flowExecution.getId();
    }

    public <T> void resumeFlow(UUID flowExecutionId, String currentAction, T context) {
        FlowExecution flowExecution = flowExecutionRegistry.findById(flowExecutionId)
                .orElseThrow(() -> new ActionException(
                        STR."Flow execution not found for id: \{flowExecutionId}"
                ));

        FlowDefinition flowDefinition = flowDefinitionRegistry.getFlowDefinition(flowExecution.getFlowId());
        if (flowDefinition == null) {
            throw new ActionException(
                    STR."Flow definition not found for flowId: \{flowExecution.getFlowId()}"
            );
        }

        ActionDefinition actionDefinition = Optional.ofNullable(flowDefinition.getAction(currentAction))
                .orElseThrow(() -> new ActionException(
                        STR."Action definition not found for action: \{currentAction} in flowId: \{flowExecution.getFlowId()}"
                ));

        if (actionDefinition.getActionType() != ActionType.RESUME) {
            throw new ActionException(
                    STR."Action definition for action: \{currentAction} in flowId: \{flowExecution.getFlowId()} is not of type RESUME"
            );
        }

        if (flowExecution.getExecutionStatus() != ExecutionStatus.SUSPENDED) {
            log.warn("Trying to resume flow execution with id: {} but its status is not SUSPENDED", flowExecutionId);
            return;
        }

        ExecutionContext executionContext = executionContextManager.loadExecutionContext(
                flowExecution,
                actionDefinition.isRemoveContextOnLoad()
        );

        FlowExecutionActionRequest<T> flowExecutionActionRequest = FlowExecutionActionRequest.<T>builder()
                .flowDefinition(flowDefinition)
                .actionDefinition(actionDefinition)
                .executionContext(executionContext)
                .payload(context)
                .build();

        executeFlowAsync(flowExecutionActionRequest, flowExecution);
    }

    public <T, P> void redirectFlow(FlowExecutionTimeout flowExecutionTimeout) {
        String resolverName = flowExecutionTimeout.timeoutDefinition().getResolver();

        NextActionResolver nextActionResolver = nextActionResolverRegistry.getNextActionResolver(resolverName);
        if (nextActionResolver == null) {
            throw new IllegalStateException("Default timeout redirect resolver not found");
        }

        FlowExecution flowExecution = flowExecutionRegistry.findById(flowExecutionTimeout.flowExecutionId())
                .orElseThrow(() -> new IllegalStateException(
                        STR."Flow execution not found for id: \{flowExecutionTimeout.flowExecutionId()}"
                ));

        if (flowExecution.getExecutionStatus() != ExecutionStatus.SUSPENDED) {
            log.warn("Trying to redirect flow execution with id: {} but its status is not SUSPENDED", flowExecutionTimeout.flowExecutionId());
            return;
        }

        FlowDefinition flowDefinition = flowDefinitionRegistry.getFlowDefinition(flowExecution.getFlowId());
        if (flowDefinition == null) {
            throw new IllegalStateException(
                    STR."Flow definition not found for flowId: \{flowExecution.getFlowId()}"
            );
        }

        ExecutionContext executionContext = executionContextManager.loadExecutionContext(
                flowExecution,
                flowExecutionTimeout.timeoutDefinition().isRemoveContextOnLoad()
        );

        P nextActionParams = (P) nextActionResolver.parseParams(flowExecutionTimeout.timeoutDefinition().getParameters());
        NextActionResolverResult nextActionResolverResult = nextActionResolver.resolveNextAction(executionContext, executionContext.getPayload(), (NextActionResolverParams) nextActionParams);

        ActionDefinition actionDefinition = flowDefinition.getAction(nextActionResolverResult.getNextAction());
        if (actionDefinition == null) {
            throw new IllegalStateException(
                    STR."Action definition not found for action: \{nextActionResolverResult.getNextAction()} in flowId: \{flowExecution.getFlowId()}"
            );
        }

        FlowExecutionActionRequest<T> flowExecutionActionRequest = FlowExecutionActionRequest.<T>builder()
                .flowDefinition(flowDefinition)
                .actionDefinition(actionDefinition)
                .executionContext(executionContext)
                .payload((T) nextActionResolverResult.getResult())
                .build();

        executeFlowAsync(flowExecutionActionRequest, flowExecution);
    }


    private void executeFlowAsync(FlowExecutionActionRequest flowExecutionActionRequest, FlowExecution flowExecution) {
        CompletableFuture.runAsync(() -> {
            FlowExecutionActionRequest<?> current = flowExecutionActionRequest;

            try {
                while (current != null) {
                    if (flowExecution.getExecutionStatus() != ExecutionStatus.EXECUTING) {
                        flowExecution.setExecutionStatus(ExecutionStatus.EXECUTING);
                        flowExecutionRegistry.updateFlowExecution(flowExecution);
                    }
                    current = flowActionExecutor.execute(current);
                }
            } catch (Exception ex) {
                log.error("Error executing async flow {}", flowExecution.getId(), ex);
            } finally {
                executionContextManager.removeExecutionContext(flowExecution.getId());
            }
        }, flowExecutorTaskExecutor);
    }


}
