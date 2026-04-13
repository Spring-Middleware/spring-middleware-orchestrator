package io.github.spring.middleware.orchestrator.core.engine.commons;

import io.github.spring.middleware.orchestrator.core.domain.commons.CommonActions;
import io.github.spring.middleware.orchestrator.core.engine.TimeoutRedirectResolver;
import io.github.spring.middleware.orchestrator.core.engine.TimeoutResolverName;
import org.springframework.stereotype.Component;

@TimeoutResolverName("errorTimeout")
@Component
public class ErrorTimeoutRedirectResolver implements TimeoutRedirectResolver<String> {

    @Override
    public String getContext() {
        return "The action execution has timed out.";
    }

    @Override
    public String redirectAction() {
        return CommonActions.ERROR;
    }

}
