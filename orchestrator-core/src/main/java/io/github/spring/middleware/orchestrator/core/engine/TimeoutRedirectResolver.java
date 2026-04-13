package io.github.spring.middleware.orchestrator.core.engine;

public interface TimeoutRedirectResolver<T> {

    T getContext();

    String redirectAction();

}
