package io.github.spring.middleware.orchestrator.core.domain;

import java.util.Map;

public class FlowDefinition {

    private FlowId flowId;
    private Map<String, ActionDefinition> actions;

    private String firstAction;

    public ActionDefinition getFirstAction() {
        return actions.get(firstAction);
    }

    public ActionDefinition getAction(String actionName) {
        return actions.get(actionName);
    }

    public FlowId getFlowId() {
        return flowId;
    }
}
