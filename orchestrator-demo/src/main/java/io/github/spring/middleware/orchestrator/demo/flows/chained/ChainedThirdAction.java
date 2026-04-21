package io.github.spring.middleware.orchestrator.demo.flows.chained;

import io.github.spring.middleware.orchestrator.core.engine.action.ActionName;
import io.github.spring.middleware.orchestrator.core.engine.action.ConsumerAction;
import io.github.spring.middleware.orchestrator.core.runtime.ActionException;
import io.github.spring.middleware.orchestrator.core.runtime.ActionExecutionContext;
import io.github.spring.middleware.orchestrator.core.runtime.ExecutionContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ActionName("THIRD_CHAINED_ACTION")
public class ChainedThirdAction implements ConsumerAction<ChainedPayload> {

    @Override
    public void consume(ExecutionContext executionContext, ActionExecutionContext actionContext, ChainedPayload chainedPayload) throws ActionException {
        log.info("Executing third chained action with payload: {}", chainedPayload.getValue());
        log.info("Data from previous action: {}", executionContext.getActionContextProperty("SECOND_CHAINED_ACTION", "DATA", String.class));
    }
}
