package io.github.spring.middleware.orchestrator.core.domain;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Builder
@Data
public class ActionDefinition {

    private String actionName;
    private ActionType actionType;

    // Instead of actionClazz, refer to the Spring Bean name (or keep actionClass if you look up bean by type)
    private String actionBeanName;

    private NextActionDefinition nextAction;
    private Map<String, Object> configuration;
    private TimeoutDefinition timeout;
    private boolean removeContextOnLoad = true;

    // Maps to "finalAction" in json
    private boolean finalAction;

}
