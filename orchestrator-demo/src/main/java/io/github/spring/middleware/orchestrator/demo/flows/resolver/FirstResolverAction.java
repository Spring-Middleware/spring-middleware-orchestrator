package io.github.spring.middleware.orchestrator.demo.flows.resolver;

import io.github.spring.middleware.orchestrator.core.engine.action.ActionName;
import io.github.spring.middleware.orchestrator.core.engine.action.FunctionAction;
import io.github.spring.middleware.orchestrator.core.runtime.ActionException;
import io.github.spring.middleware.orchestrator.core.runtime.ActionExecutionContext;
import io.github.spring.middleware.orchestrator.core.runtime.ExecutionContext;

import java.util.UUID;

@ActionName("FIRST_RESOLVER_ACTION")
public class FirstResolverAction implements FunctionAction<FlowInput,FlowInput> {

    @Override
    public FlowInput apply(ExecutionContext executionContext, ActionExecutionContext actionExecutionContext, FlowInput payload) throws ActionException {
        payload.setFlowInputId(UUID.randomUUID());
        return payload;
    }
}
