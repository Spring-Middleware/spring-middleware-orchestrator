package io.github.spring.middleware.orchestrator.core.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlowDefinition {

    private FlowId flowId;
    private List<ActionDefinition> actions;
    private Map<String, ActionDefinition> actionDefinitionMap;

    private String firstAction;

    public ActionDefinition getFirstAction() {
        return actionDefinitionMap.get(firstAction);
    }

    public ActionDefinition getAction(String actionName) {
        return actionDefinitionMap.get(actionName);
    }

    public boolean hasAction(String actionName) {
        return actionDefinitionMap.containsKey(actionName);
    }

    public FlowId getFlowId() {
        return flowId;
    }

    public void buildActionDefinitionMap() {
        actionDefinitionMap = actions.stream()
                .collect(java.util.stream.Collectors.toMap(ActionDefinition::getActionName, a -> a));
    }
}
