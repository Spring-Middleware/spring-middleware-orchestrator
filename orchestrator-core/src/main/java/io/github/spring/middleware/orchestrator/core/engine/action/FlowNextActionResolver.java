package io.github.spring.middleware.orchestrator.core.engine.action;

import io.github.spring.middleware.orchestrator.core.domain.ActionDefinition;
import io.github.spring.middleware.orchestrator.core.domain.FlowDefinition;
import io.github.spring.middleware.orchestrator.core.domain.NextActionDefinition;
import io.github.spring.middleware.orchestrator.core.engine.NextActionResolver;
import io.github.spring.middleware.orchestrator.core.port.NextActionResolverRegistry;
import io.github.spring.middleware.orchestrator.core.runtime.ExecutionContext;
import io.github.spring.middleware.orchestrator.core.runtime.FlowExecutionNextAction;
import io.github.spring.middleware.orchestrator.core.runtime.NextActionResolverResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FlowNextActionResolver {

    private final NextActionResolverRegistry nextActionResolverRegistry;

    public <R> FlowExecutionNextAction getNextAction(
            FlowDefinition flowDefinition,
            ExecutionContext executionContext,
            String currentActionName,
            R result
    ) {
        ActionDefinition currentActionDefinition = flowDefinition.getAction(currentActionName);
        if (currentActionDefinition == null) {
            throw new IllegalStateException(
                    String.format("Action '%s' not found in flow definition", currentActionName)
            );
        }
        NextActionDefinition nextActionDefinition = currentActionDefinition.getNextAction();
        if (nextActionDefinition == null) {
            throw new IllegalStateException(
                    String.format("Next action definition not found for action '%s'", currentActionName)
            );
        }

        NextActionResolverResult nextActionResolverResult = resolveNextActionResult(executionContext, result, nextActionDefinition);
        String nextActionName = nextActionResolverResult.getNextAction();
        if (nextActionName == null || !flowDefinition.hasAction(nextActionName)) {
            throw new IllegalStateException(
                    String.format("Next action '%s' not found in flow definition", nextActionName));
        }

        return FlowExecutionNextAction.builder()
                .actionDefinition(flowDefinition.getAction(nextActionName))
                .nextActionResolverResult(nextActionResolverResult).build();
    }


    private <R> NextActionResolverResult resolveNextActionResult(ExecutionContext executionContext, R result,
                                                                 NextActionDefinition nextActionDefinition) {
        NextActionResolver nextActionResolver = nextActionResolverRegistry.getNextActionResolver(nextActionDefinition.getResolver());
        return nextActionResolver.resolveNextAction(executionContext, result, nextActionResolver.parseParams(nextActionDefinition.getParameters()));
    }


}
