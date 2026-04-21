package io.github.spring.middleware.orchestrator.demo.flows.resume;

import io.github.spring.middleware.orchestrator.core.engine.action.ActionName;
import io.github.spring.middleware.orchestrator.core.engine.action.FunctionAction;
import io.github.spring.middleware.orchestrator.core.runtime.ActionException;
import io.github.spring.middleware.orchestrator.core.runtime.ActionExecutionContext;
import io.github.spring.middleware.orchestrator.core.runtime.ExecutionContext;

import java.util.Random;
import java.util.stream.IntStream;

@ActionName("CREATE_CONTEXT_ACTION")
public class CreateContextAction implements FunctionAction<ContextInput, ContextPayload> {

    private final Random random = new Random();

    @Override
    public ContextPayload apply(ExecutionContext executionContext, ActionExecutionContext actionExecutionContext, ContextInput contextInput) throws ActionException {
        final var contextPayload = new ContextPayload();
        contextPayload.setFlowExecutionId(executionContext.getFlowExecution().getId());
        contextPayload.setKey(contextInput.getKey());
        contextPayload.setResumeType(contextInput.getResumeType());
        IntStream.range(0, random.nextInt(1, 5)).mapToObj(i -> {
            Step step = new Step();
            step.setRandom(random.nextInt(100));
            step.setText(STR."Step \{i}");
            return step;
        }).forEach(contextPayload.getSteps()::add);
        return contextPayload;
    }
}
