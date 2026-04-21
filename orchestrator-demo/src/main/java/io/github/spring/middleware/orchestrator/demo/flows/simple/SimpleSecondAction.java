package io.github.spring.middleware.orchestrator.demo.flows.simple;

import io.github.spring.middleware.orchestrator.core.engine.action.ActionName;
import io.github.spring.middleware.orchestrator.core.engine.action.ConsumerAction;
import io.github.spring.middleware.orchestrator.core.runtime.ActionException;
import io.github.spring.middleware.orchestrator.core.runtime.ActionExecutionContext;
import io.github.spring.middleware.orchestrator.core.runtime.ExecutionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@ActionName("SECOND_ACTION")
public class SimpleSecondAction implements ConsumerAction<SimplePayload> {

    @Override
    public void consume(ExecutionContext executionContext, ActionExecutionContext actionContext, SimplePayload simplePayload) throws ActionException {
       log.info("Executing second action with payload: {}", simplePayload);
    }
}
