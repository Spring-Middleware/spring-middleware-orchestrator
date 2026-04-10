package io.github.spring.middleware.orchestrator.core.port;

import io.github.spring.middleware.orchestrator.core.engine.Action;

public interface ActionRegistry {

    <T extends Action> T getAction(String actionName, Class<T> actionType);
}
