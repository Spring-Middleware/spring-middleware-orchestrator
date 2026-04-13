package io.github.spring.middleware.orchestrator.infra.engine.registry;

import io.github.spring.middleware.orchestrator.core.engine.action.Action;
import io.github.spring.middleware.orchestrator.core.engine.action.ActionName;
import io.github.spring.middleware.orchestrator.core.port.ActionRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DefaultActionRegistry implements ActionRegistry {

    private final List<Action> actions;

    private final Map<String, Action> actionsByName = new HashMap<>();

    @PostConstruct
    public void init() {
        for (Action action : actions) {
            Class<?> clazz = action.getClass();

            ActionName annotation = clazz.getAnnotation(ActionName.class);
            if (annotation == null) {
                throw new IllegalStateException(
                        STR."Action \{clazz.getName()} must be annotated with @ActionName");
            }

            String name = annotation.value();

            if (actionsByName.containsKey(name)) {
                throw new IllegalStateException(
                        STR."Duplicate ActionName detected: \{name}");
            }

            actionsByName.put(name, action);
        }
    }


    @Override
    @SuppressWarnings("unchecked")
    public <T extends Action> T getAction(String actionName, Class<T> actionType) {

        Action action = actionsByName.get(actionName);

        if (action == null) {
            throw new IllegalArgumentException(
                    STR."No Action found for name: \{actionName}");
        }

        if (!actionType.isAssignableFrom(action.getClass())) {
            throw new IllegalArgumentException(
                    STR."Action \{actionName} is not of type \{actionType.getName()}");
        }

        return (T) action;
    }
}
