package io.github.spring.middleware.orchestrator.core.runtime;

import io.github.spring.middleware.orchestrator.core.domain.ActionDefinition;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FlowExecutionNextAction {

    private ActionDefinition actionDefinition;
    private NextActionResolverResult nextActionResolverResult;
}
