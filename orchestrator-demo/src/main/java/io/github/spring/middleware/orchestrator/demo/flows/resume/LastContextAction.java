package io.github.spring.middleware.orchestrator.demo.flows.resume;

import io.github.spring.middleware.orchestrator.core.engine.action.ActionName;
import io.github.spring.middleware.orchestrator.core.engine.action.ConsumerAction;
import io.github.spring.middleware.orchestrator.core.runtime.ActionException;
import io.github.spring.middleware.orchestrator.core.runtime.ActionExecutionContext;
import io.github.spring.middleware.orchestrator.core.runtime.ExecutionContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ActionName("LAST_CONTEXT_ACTION")
public class LastContextAction implements ConsumerAction<Void> {

    @Override
    public void consume(ExecutionContext<?> executionContext, ActionExecutionContext actionContext, Void input) throws ActionException {
        ContextPayload contextPayload = executionContext.get("contextPayload", ContextPayload.class);
        log.info(STR."Finish flow \{contextPayload.getFlowExecutionId()}");
        contextPayload.getSteps().stream().forEach(step -> {
            log.info(STR."\{step.getText()}  rand: \{step.getRandom()}");
        });
    }
}