package io.github.spring.middleware.orchestrator.demo.flows.chained;

import io.github.spring.middleware.orchestrator.core.engine.action.ActionName;
import io.github.spring.middleware.orchestrator.core.engine.action.FunctionAction;
import io.github.spring.middleware.orchestrator.core.runtime.ActionException;
import io.github.spring.middleware.orchestrator.core.runtime.ActionExecutionContext;
import io.github.spring.middleware.orchestrator.core.runtime.ExecutionContext;

import java.util.UUID;

@ActionName("SECOND_CHAINED_ACTION")
public class ChainedSecondAction implements FunctionAction<ChainedPayload, ChainedPayload> {

    @Override
    public ChainedPayload apply(ExecutionContext executionContext, ActionExecutionContext actionExecutionContext, ChainedPayload chainedPayload) throws ActionException {
        if (chainedPayload.getType() == ChainedType.SUCCESS) {
            chainedPayload.setValue(UUID.randomUUID().toString());
            actionExecutionContext.put("DATA", "MY_DATA");
        } else {
            throw new ActionException("Chained action failed");
        }
        return chainedPayload;
    }
}
