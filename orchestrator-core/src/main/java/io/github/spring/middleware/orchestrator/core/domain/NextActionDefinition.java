package io.github.spring.middleware.orchestrator.core.domain;

import io.github.spring.middleware.orchestrator.core.domain.params.NextActionResolverParams;

public class NextActionDefinition<P extends NextActionResolverParams> {

    private String resolver; // antes clazz
    private P params;

}
