package io.github.spring.middleware.orchestrator.core.engine.commons;

import io.github.spring.middleware.orchestrator.core.engine.action.ActionName;
import io.github.spring.middleware.orchestrator.core.engine.action.ConsumerAction;
import io.github.spring.middleware.orchestrator.core.runtime.ActionException;
import io.github.spring.middleware.orchestrator.core.runtime.ActionExecutionContext;
import io.github.spring.middleware.orchestrator.core.runtime.ExecutionContext;
import org.springframework.beans.factory.annotation.Value;

@ActionName("ERROR")
public class ErrorAction<T> implements ConsumerAction<T> {

    @Value("${middleware.orchestrator.error.persist-result:false}")
    private boolean persisResult;

    @Override
    public void consume(ExecutionContext<?> executionContext, ActionExecutionContext actionContext, T t) throws ActionException {
        if (persisResult) {
            actionContext.put("result", t);
        }
        throw new ActionException("Error action executed");
    }
}
