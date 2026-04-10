package io.github.spring.middleware.orchestrator.core.engine.commons;

import io.github.spring.middleware.orchestrator.core.engine.ResolverName;
import io.github.spring.middleware.orchestrator.core.engine.TimeoutRedirectResolver;
import io.github.spring.middleware.orchestrator.core.domain.commons.CommonActions;
import org.springframework.stereotype.Component;

@ResolverName("errorTimeout")
@Component
public class ErrorTimeoutRedirectResolver implements TimeoutRedirectResolver<String> {

    @Override
    public String get() {
        return "The action execution has timed out.";
    }

    @Override
    public String redirectAction() {
        return CommonActions.ERROR;
    }

}
