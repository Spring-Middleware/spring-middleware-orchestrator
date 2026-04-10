package io.github.spring.middleware.orchestrator.core.engine;

public interface TimeoutRedirectResolverRegistry {

    TimeoutRedirectResolver get(String name);

}
