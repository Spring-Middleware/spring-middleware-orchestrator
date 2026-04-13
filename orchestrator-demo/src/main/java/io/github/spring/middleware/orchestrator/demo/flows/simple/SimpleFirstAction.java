package io.github.spring.middleware.orchestrator.demo.flows.simple;

import io.github.spring.middleware.orchestrator.core.engine.action.ActionName;
import io.github.spring.middleware.orchestrator.core.engine.action.FunctionAction;
import io.github.spring.middleware.orchestrator.core.runtime.ActionException;
import io.github.spring.middleware.orchestrator.core.runtime.ActionExecutionContext;
import io.github.spring.middleware.orchestrator.core.runtime.ExecutionContext;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ActionName("FIRST_ACTION")
public class SimpleFirstAction implements FunctionAction<Void, SimplePayload> {

    private String name;
    private Integer number;
    private List<Integer> array;

    @Override
    public SimplePayload apply(ExecutionContext executionContext, ActionExecutionContext actionExecutionContext, Void payload) throws ActionException {
        final var result = new SimplePayload();
        result.setName(name);
        result.setNumber(number);
        result.setArray(array);
        return result;
    }

}
