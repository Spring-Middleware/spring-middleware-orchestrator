package io.github.spring.middleware.orchestrator.core.engine;

import io.github.spring.middleware.orchestrator.core.domain.params.NextActionResolverParams;
import io.github.spring.middleware.orchestrator.core.runtime.ExecutionContext;
import io.github.spring.middleware.orchestrator.core.runtime.NextActionResolverResult;

public interface NextActionResolver<T, P extends NextActionResolverParams> {

    NextActionResolverResult resolveNextAction(ExecutionContext executionContext, T actionResult, P nextActionParams);

}
