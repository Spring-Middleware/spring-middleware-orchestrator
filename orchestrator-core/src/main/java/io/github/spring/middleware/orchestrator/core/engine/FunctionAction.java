package io.github.spring.middleware.orchestrator.core.engine;

import io.github.spring.middleware.orchestrator.core.runtime.ActionException;
import io.github.spring.middleware.orchestrator.core.runtime.ActionExecutionContext;
import io.github.spring.middleware.orchestrator.core.runtime.ExecutionContext;

@FunctionalInterface
public interface FunctionAction<E, R> extends Action {

    R apply(ExecutionContext executionContext, ActionExecutionContext actionExecutionContext, E e) throws ActionException;
}
