package io.github.spring.middleware.orchestrator.core.engine.action;

import io.github.spring.middleware.orchestrator.core.runtime.ActionException;
import io.github.spring.middleware.orchestrator.core.runtime.ActionExecutionContext;
import io.github.spring.middleware.orchestrator.core.runtime.ExecutionContext;

import java.util.Map;

public interface FunctionAction<T, R> extends Action {

    default T parsePayload(Object payload) throws ActionException {
        return PayloadParserUtils.parse(payload,this, FunctionAction.class, 0);
    }

    R apply(ExecutionContext<?> executionContext, ActionExecutionContext actionExecutionContext, T payload) throws ActionException;
}
