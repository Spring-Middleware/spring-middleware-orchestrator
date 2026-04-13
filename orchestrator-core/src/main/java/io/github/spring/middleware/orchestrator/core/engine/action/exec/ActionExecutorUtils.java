package io.github.spring.middleware.orchestrator.core.engine.action.exec;

import io.github.spring.middleware.orchestrator.core.domain.ActionDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class ActionExecutorUtils {

    private static final Logger log = LoggerFactory.getLogger(ActionExecutorUtils.class);

    private ActionExecutorUtils() {
    }

    public static void configureAction(Object action, ActionDefinition actionDefinition) {
        Map<String, Object> configuration =
                Optional.ofNullable(actionDefinition.getConfiguration()).orElse(Map.of());

        if (configuration.isEmpty()) {
            return;
        }

        Class<?> actionClass = action.getClass();

        try {
            Map<String, Method> settersByProperty = Arrays.stream(Introspector.getBeanInfo(actionClass)
                    .getPropertyDescriptors())
                    .sequential()
                    .filter(pd -> pd.getWriteMethod() != null)
                    .collect(Collectors.toMap(
                            PropertyDescriptor::getName,
                            PropertyDescriptor::getWriteMethod,
                            (a, b) -> a
                    ));

            for (Map.Entry<String, Object> entry : configuration.entrySet()) {
                String property = entry.getKey();
                Object value = entry.getValue();

                Method setter = settersByProperty.get(property);

                if (setter == null) {
                    log.warn("No setter found for property '{}' in action {}", property, actionClass.getName());
                    continue;
                }

                try {
                    setter.invoke(action, value);
                } catch (Exception e) {
                    log.error("Error configuring action {} for property {}", actionClass.getName(), property, e);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(STR."Error introspecting action \{actionClass.getName()}", e);
        }
    }
}