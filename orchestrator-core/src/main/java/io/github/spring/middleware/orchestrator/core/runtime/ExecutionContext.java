package io.github.spring.middleware.orchestrator.core.runtime;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class ExecutionContext<P> {

    private FlowExecution flowExecution;
    private final LocalDateTime creationDateTime;
    private final Map<String, Object> runtimeContext;
    private final P payload;
    private final AtomicInteger executionCounter = new AtomicInteger(0);

    public ExecutionContext(FlowExecution flowExecution, P payload) {

        this.flowExecution = flowExecution;
        this.runtimeContext = new HashMap<>();
        this.creationDateTime = LocalDateTime.now();
        this.payload = payload;
    }


    public void put(String propertyName, Object data) {

        runtimeContext.put(propertyName, data);
    }

    public <T> T get(String propertyName, Class<T> clazz) {
        Object value = runtimeContext.get(propertyName);

        if (value == null) {
            return null; // o lanza excepción si no debería ser null
        }

        if (!clazz.isInstance(value)) {
            throw new IllegalStateException(
                    "Property '%s' is of type %s, not %s"
                            .formatted(propertyName, value.getClass().getName(), clazz.getName())
            );
        }

        return clazz.cast(value);
    }

    public void addActionExecution(ActionExecution actionExecution) {

        flowExecution.addActionExecution(new ActionExecutionOrder(executionCounter.incrementAndGet(), actionExecution));
    }

    public P getPayload() {
        return payload;
    }

    public FlowExecution getFlowExecution() {

        return flowExecution;
    }

    public Map<String, Object> getRuntimeContext() {
        return runtimeContext;
    }

    public <T> T getRequiredActionContextProperty(String actionName, String propertyName, Class<T> type) {
        T value = getActionContextProperty(actionName, propertyName, type);
        if (value == null) {
            throw new IllegalStateException(
                    "Property '%s' from action '%s' was not found"
                            .formatted(propertyName, actionName)
            );
        }
        return value;
    }

    public <T, C> T getActionContextProperty(String actionName, String propertyName, Class<T> type) {
        FlowExecution<T, C> tFlowExecution = flowExecution;
        Object value = Optional.ofNullable(tFlowExecution.getActionExecution(actionName))
                .map(actionExecution -> actionExecution.getContext().get(propertyName))
                .orElse(null);

        if (value == null) {
            return null;
        }

        if (!type.isInstance(value)) {
            throw new IllegalStateException(
                    "Property '%s' from action '%s' is of type %s, not %s"
                            .formatted(propertyName, actionName, value.getClass().getName(), type.getName())
            );
        }

        return type.cast(value);
    }
}
