package io.github.spring.middleware.orchestrator.core.port;

import io.github.spring.middleware.orchestrator.core.domain.params.NextActionResolverParams;
import io.github.spring.middleware.orchestrator.core.engine.NextActionResolver;

public interface NextActionResolverRegistry<T, P extends NextActionResolverParams> {

    NextActionResolver<T, P> getNextActionResolver(String resolverName);

}
