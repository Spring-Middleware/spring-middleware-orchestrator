package io.github.spring.middleware.orchestrator.core.domain;

import io.github.spring.middleware.orchestrator.core.domain.commons.CommonActions;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActionDefinition {

    private String actionName;
    private ActionType actionType;

    private NextActionDefinition nextAction;
    private Map<String, Object> configuration;
    private TimeoutDefinition timeout;
    private boolean removeContextOnLoad = true;

    // Maps to "finalAction" in json
    private boolean finalAction;

    public static ActionDefinition ERROR_ACTION = new ActionDefinition(
            CommonActions.ERROR,
            ActionType.CONSUMER,
            null,
            null,
            null,
            false,
            true
    );

    public static ActionDefinition END_ACTION = new ActionDefinition(
            CommonActions.END,
            ActionType.CONSUMER,
            null,
            null,
            null,
            false,
            true
    );
}
