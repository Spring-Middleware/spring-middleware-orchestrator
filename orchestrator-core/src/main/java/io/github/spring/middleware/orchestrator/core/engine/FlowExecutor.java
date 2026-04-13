package io.github.spring.middleware.orchestrator.core.engine;

import io.github.spring.middleware.orchestrator.core.domain.ActionDefinition;
import io.github.spring.middleware.orchestrator.core.domain.ActionType;
import io.github.spring.middleware.orchestrator.core.domain.FlowDefinition;
import io.github.spring.middleware.orchestrator.core.domain.FlowId;
import io.github.spring.middleware.orchestrator.core.engine.action.FlowExecutionActionRequest;
import io.github.spring.middleware.orchestrator.core.engine.action.exec.FlowActionExecutor;
import io.github.spring.middleware.orchestrator.core.port.FlowExecutionRegistry;
import io.github.spring.middleware.orchestrator.core.runtime.ActionException;
import io.github.spring.middleware.orchestrator.core.runtime.ExecutionContext;
import io.github.spring.middleware.orchestrator.core.runtime.FlowExecution;
import io.github.spring.middleware.orchestrator.core.runtime.FlowExecutionTimeout;
import io.github.spring.middleware.orchestrator.core.runtime.FlowTrigger;
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
    private final TimeoutRedirectResolverRegistry timeoutRedirectResolverRegistry;
    private final Executor flowExecutorTaskExecutor;


    public UUID startFlow(FlowTrigger flowTrigger) {
        FlowExecution flowExecution = flowExecutionRegistry.createFlowExecution(
                flowTrigger.getFlowId(),
                flowTrigger.getPayload()
        );

        ExecutionContext executionContext = new ExecutionContext(flowExecution);
        executionContextManager.addExecutionContext(executionContext);

        FlowDefinition flowDefinition = flowDefinitionRegistry.getFlowDefinition(
                new FlowId(flowTrigger.getFlowId())
        );

        if (flowDefinition == null) {
            executionContextManager.removeExecutionContext(flowExecution.getId());
            throw new IllegalStateException(
                    STR."Flow definition not found for flowId: \{flowTrigger.getFlowId()}"
            );
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

        CompletableFuture.runAsync(() -> {
            FlowExecutionActionRequest<?> current = flowExecutionActionRequest;

            try {
                while (current != null) {
                    current = flowActionExecutor.execute(current);
                }
            } catch (Exception ex) {
                log.error("Error executing async flow {}", flowExecution.getId(), ex);
            } finally {
                executionContextManager.removeExecutionContext(flowExecution.getId());
            }
        }, flowExecutorTaskExecutor);

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

        CompletableFuture.runAsync(() -> {
            FlowExecutionActionRequest<?> current = flowExecutionActionRequest;

            try {
                while (current != null) {
                    current = flowActionExecutor.execute(current);
                }
            } catch (Exception ex) {
                log.error("Error executing async flow {}", flowExecution.getId(), ex);
            } finally {
                executionContextManager.removeExecutionContext(flowExecution.getId());
            }
        }, flowExecutorTaskExecutor);
    }

    public <T> void redirectFlow(FlowExecutionTimeout flowExecutionTimeout) {
        String resolverName = flowExecutionTimeout.timeoutDefinition().getOnTimeoutResolver();

        TimeoutRedirectResolver timeoutRedirectResolver = timeoutRedirectResolverRegistry.get(resolverName);
        if (timeoutRedirectResolver == null) {
            throw new IllegalStateException("Default timeout redirect resolver not found");
        }

        resumeFlow(
                flowExecutionTimeout.flowExecutionId(),
                timeoutRedirectResolver.redirectAction(),
                timeoutRedirectResolver.getContext()
        );
    }


}
