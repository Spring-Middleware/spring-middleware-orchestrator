package io.github.spring.middleware.orchestrator.core.engine;

import io.github.spring.middleware.orchestrator.core.domain.FlowDefinition;
import io.github.spring.middleware.orchestrator.core.domain.FlowId;
import io.github.spring.middleware.orchestrator.core.runtime.ExecutionContext;
import io.github.spring.middleware.orchestrator.core.runtime.FlowExecution;
import io.github.spring.middleware.orchestrator.core.runtime.FlowTrigger;
import io.github.spring.middleware.orchestrator.core.port.ActionRegistry;
import io.github.spring.middleware.orchestrator.core.port.FlowExecutionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class FlowExecutor {

    private final FlowDefinitionRegistry flowDefinitionRegistry;
    private final ActionRegistry actionRegistry;
    private final FlowExecutionRegistry flowExecutionRegistry;
    private final ExecutionContextManager executionContextManager;



    public UUID startFlow(FlowTrigger flowTrigger) {
        FlowExecution flowExecution = flowExecutionRegistry.createFlowExecution(flowTrigger.getFlowId(), flowTrigger.getPayload());
        ExecutionContext executionContext = new ExecutionContext(flowExecution);
        executionContextManager.addExecutionContext(executionContext);
        FlowDefinition flowDefinition = flowDefinitionRegistry.getFlowDefinition(new FlowId(flowTrigger.getFlowId()));




    }





}
