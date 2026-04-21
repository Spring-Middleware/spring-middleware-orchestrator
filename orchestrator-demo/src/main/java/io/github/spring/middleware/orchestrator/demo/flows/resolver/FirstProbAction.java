package io.github.spring.middleware.orchestrator.demo.flows.resolver;

import io.github.spring.middleware.orchestrator.core.engine.action.ActionName;
import io.github.spring.middleware.orchestrator.core.engine.action.ConsumerAction;
import io.github.spring.middleware.orchestrator.core.runtime.ActionException;
import io.github.spring.middleware.orchestrator.core.runtime.ActionExecutionContext;
import io.github.spring.middleware.orchestrator.core.runtime.ExecutionContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ActionName("FIRST_PROB_ACTION")
public class FirstProbAction implements ConsumerAction<String> {

    @Override
    public void consume(ExecutionContext executionContext, ActionExecutionContext actionContext, String s) throws ActionException {
        actionContext.put("rand", executionContext.get("rand", Double.class));
        log.info("Executing FirstProbAction with input: {}", s);
    }
}
