package io.github.spring.middleware.orchestrator.core.runtime;

import io.github.spring.middleware.orchestrator.core.domain.FlowDefinition;

public class FlowExecutionActionParams {

    private ExecutionContext executionContext;
    private FlowDefinition flowDefinition;
    private EventActionDefinition eventActionDefinition;
    private T context;

    public EventActionType getActionType() {

        return eventActionDefinition.getActionType();
    }

    public String getActionClazz() {

        return eventActionDefinition.getActionClazz();
    }

    public EventContext getEventContext() {

        return eventContext;
    }

    public <P> EventNextAction getNextAction(P result) {

        return eventDefinition.getNextAction(eventContext, result, eventActionDefinition.getActionName());
    }

}
