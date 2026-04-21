package io.github.spring.middleware.orchestrator.core.engine.commons;

import io.github.spring.middleware.orchestrator.core.engine.action.ActionName;
import io.github.spring.middleware.orchestrator.core.engine.action.ConsumerAction;
import io.github.spring.middleware.orchestrator.core.runtime.ActionException;
import io.github.spring.middleware.orchestrator.core.runtime.ActionExecutionContext;
import io.github.spring.middleware.orchestrator.core.runtime.ExecutionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@ActionName("END")
public class EndAction<T> implements ConsumerAction<T> {

    @Value("${middleware.orchestrator.end.persist-result:false}")
    private boolean persisResult;

    @Override
    public void consume(ExecutionContext<?> executionContext, ActionExecutionContext actionContext, T t) throws ActionException {
        log.info("End action reached with result: {}", t);
        if (persisResult) {
            actionContext.put("result", t);
        }
    }
}
