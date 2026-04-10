package io.github.spring.middleware.orchestrator.core.runtime;

public class ActionException extends RuntimeException {

    private final String actionName;

    public ActionException(String actionName, String message) {
        super(message);
        this.actionName = actionName;
    }

    public String getActionName() {
        return actionName;
    }

}
