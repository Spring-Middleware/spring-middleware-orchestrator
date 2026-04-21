package io.github.spring.middleware.orchestrator.core.engine.action;

import io.github.spring.middleware.orchestrator.core.runtime.ActionException;
import io.github.spring.middleware.orchestrator.core.runtime.ActionExecutionContext;
import io.github.spring.middleware.orchestrator.core.runtime.ExecutionContext;

public interface ConsumerAction<C> extends Action {

    default C parsePayload(Object payload) throws ActionException {
        return PayloadParserUtils.parse(payload, this, ConsumerAction.class, 0);
    }

    void consume(ExecutionContext<?> executionContext, ActionExecutionContext actionContext, C c) throws ActionException;

}
