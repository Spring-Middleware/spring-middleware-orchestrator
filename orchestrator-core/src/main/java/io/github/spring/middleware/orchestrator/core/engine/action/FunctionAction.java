package io.github.spring.middleware.orchestrator.core.engine.action;

import io.github.spring.middleware.orchestrator.core.runtime.ActionException;
import io.github.spring.middleware.orchestrator.core.runtime.ActionExecutionContext;
import io.github.spring.middleware.orchestrator.core.runtime.ExecutionContext;

import java.util.Map;

@FunctionalInterface
public interface FunctionAction<T, R> extends Action {

    R apply(ExecutionContext executionContext, ActionExecutionContext actionExecutionContext, T payload) throws ActionException;
}
