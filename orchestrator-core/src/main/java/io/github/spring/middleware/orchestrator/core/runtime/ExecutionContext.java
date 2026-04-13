package io.github.spring.middleware.orchestrator.core.runtime;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class ExecutionContext {

    private FlowExecution flowExecution;
    private final LocalDateTime creationDateTime;
    private final Map<String, Object> runtimeContext;

    public ExecutionContext(FlowExecution flowExecution) {

        this.flowExecution = flowExecution;
        this.runtimeContext = new HashMap<>();
        this.creationDateTime = LocalDateTime.now();
    }


    public void put(String propertyName, Object data) {

        runtimeContext.put(propertyName, data);
    }

    public <T> T get(String propertyName) {

        return (T) runtimeContext.get(propertyName);
    }

    public void addActionExecution(ActionExecution actionExecution) {

        flowExecution.addActionExecution(actionExecution);
    }


    public FlowExecution getFlowExecution() {

        return flowExecution;
    }

    public Map<String, Object> getRuntimeContext() {
        return runtimeContext;
    }
}
