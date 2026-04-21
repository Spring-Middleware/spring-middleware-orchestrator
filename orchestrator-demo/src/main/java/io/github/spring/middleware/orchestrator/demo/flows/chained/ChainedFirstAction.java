package io.github.spring.middleware.orchestrator.demo.flows.chained;

import io.github.spring.middleware.orchestrator.core.engine.action.ActionName;
import io.github.spring.middleware.orchestrator.core.engine.action.FunctionAction;
import io.github.spring.middleware.orchestrator.core.runtime.ActionException;
import io.github.spring.middleware.orchestrator.core.runtime.ActionExecutionContext;
import io.github.spring.middleware.orchestrator.core.runtime.ExecutionContext;
import org.springframework.stereotype.Component;


@ActionName("FIRST_CHAINED_ACTION")
public class ChainedFirstAction implements FunctionAction<ChainedType, ChainedPayload> {

    @Override
    public ChainedPayload apply(ExecutionContext executionContext, ActionExecutionContext actionExecutionContext, ChainedType chainedType) throws ActionException {
        final var result = new ChainedPayload();
        result.setType(chainedType);
        return result;
    }
}
