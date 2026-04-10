package io.github.spring.middleware.orchestrator.core.engine;

import io.github.spring.middleware.orchestrator.core.runtime.ActionException;
import io.github.spring.middleware.orchestrator.core.runtime.ActionExecutionContext;
import io.github.spring.middleware.orchestrator.core.runtime.ExecutionContext;

public interface ConsumerAction<C> extends Action {

    void consume(ExecutionContext executionContext, ActionExecutionContext actionContext, C c) throws ActionException;

}
